package org.kexon5.recipes.controller;

import org.kexon5.recipes.model.Recipe;
import org.kexon5.recipes.model.User;
import org.kexon5.recipes.model.UserDetailsImpl;
import org.kexon5.recipes.service.RecipeService;
import org.kexon5.recipes.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class MainController {
    @Autowired
    private final RecipeService recipeService;

    @Autowired
    private final UserDetailsServiceImpl userDetailsService;

    public MainController(RecipeService recipeService, UserDetailsServiceImpl userDetailsService) {
        this.recipeService = recipeService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/api/register")
    public void register(@Validated @RequestBody User user) {
        userDetailsService.createUser(user);
    }

    @PostMapping("/api/recipe/new")
    public Map.Entry<String, Long> setRecipe(@AuthenticationPrincipal UserDetailsImpl userDetails, @Validated @RequestBody Recipe recipe) {
        return recipeService.createRecipe(userDetails, recipe);
    }

    @GetMapping("/api/recipe/{id}")
    public Recipe getRecipe(@PathVariable long id) {
        return recipeService.getRecipeById(id);
    }

    @GetMapping("/api/recipe/search")
    public List<Recipe> getRecipes(@RequestParam(required = false) String category, @RequestParam(required = false) String name) {
        return recipeService.getRecipesByCategoryOrName(category, name);
    }

    @PutMapping("/api/recipe/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void putRecipe(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable long id, @Validated @RequestBody Recipe recipe) {
        recipeService.updateRecipeById(userDetails, id, recipe);
    }

    @DeleteMapping("/api/recipe/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteRecipe(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable long id) {
        recipeService.deleteRecipeById(userDetails, id);
    }
}
