package org.kexon5.recipes.repository;

import org.kexon5.recipes.model.Recipe;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends CrudRepository<Recipe, Long> {
    Recipe findRecipeById(Long id);
    Optional<List<Recipe>> getRecipesByCategoryIgnoreCase(String category);
    Optional<List<Recipe>> getRecipesByNameContainsIgnoreCase(String name);
    boolean existsRecipeByIdAndUserId(Long id, Long userId);
}
