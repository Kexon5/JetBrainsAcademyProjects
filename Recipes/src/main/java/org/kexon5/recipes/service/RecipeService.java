package org.kexon5.recipes.service;

import org.kexon5.recipes.model.Recipe;
import org.kexon5.recipes.model.UserDetailsImpl;
import org.kexon5.recipes.repository.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    @Autowired
    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Map.Entry<String, Long> createRecipe(UserDetailsImpl userDetails, Recipe recipe) {
        recipe.setDate(LocalDateTime.now());
        recipe.setUserId(userDetails.getId());
        recipeRepository.save(recipe);
        return Map.entry("id", recipe.getId());
    }

    public Recipe getRecipeById(long id) {
        if (recipeRepository.existsById(id)) {
            return recipeRepository.findRecipeById(id);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public List<Recipe> getRecipesByCategoryOrName(String category, String name) {
        if (category == null && name == null || category != null && name != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        List<Recipe> recipeList = category != null ? getRecipesByCategoryIgnoreCase(category) :
                getRecipesByNameContainsIgnoreCase(name);
        return orderRecipesByNewer(recipeList);
    }

    public List<Recipe> getRecipesByCategoryIgnoreCase(String category) {
        return recipeRepository.getRecipesByCategoryIgnoreCase(category).orElse(null);
    }

    public List<Recipe> getRecipesByNameContainsIgnoreCase(String name) {
        return recipeRepository.getRecipesByNameContainsIgnoreCase(name).orElse(null);
    }

    private List<Recipe> orderRecipesByNewer(List<Recipe> recipes) {
        return recipes != null ? recipes.stream()
                .sorted(Comparator.comparing(Recipe::getDate).reversed())
                .collect(Collectors.toList()) : null;
    }

    public void updateRecipeById(UserDetailsImpl userDetails, long id, Recipe recipe) {
        if (recipeRepository.existsById(id)) {
            if (recipeRepository.existsRecipeByIdAndUserId(id, userDetails.getId())) {
                recipe.setDate(LocalDateTime.now());
                recipe.setId(id);
                recipe.setUserId(userDetails.getId());
                recipeRepository.save(recipe);
                return;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    public void deleteRecipeById(UserDetailsImpl userDetails, long id) {
        if (recipeRepository.existsById(id)) {
            if (recipeRepository.existsRecipeByIdAndUserId(id, userDetails.getId())) {
                recipeRepository.deleteById(id);
                return;
            } else {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }
}
