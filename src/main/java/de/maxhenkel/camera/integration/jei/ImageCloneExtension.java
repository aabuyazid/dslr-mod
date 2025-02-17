package de.maxhenkel.camera.integration.jei;

import de.maxhenkel.camera.ImageCloningRecipe;
import de.maxhenkel.camera.ImageData;
import de.maxhenkel.camera.Main;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.ingredient.ICraftingGridHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.extensions.vanilla.crafting.ICraftingCategoryExtension;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ImageCloneExtension<T extends ImageCloningRecipe> implements ICraftingCategoryExtension {

    private final T recipe;

    public ImageCloneExtension(T recipe) {
        this.recipe = recipe;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ICraftingGridHelper craftingGridHelper, IFocusGroup focuses) {
        ItemStack image = new ItemStack(Main.IMAGE.get());
        ImageData.dummy().addToImage(image);

        List<ItemStack> paper = Arrays.asList(recipe.getPaper().getItems());

        ItemStack out = image.copy();

        craftingGridHelper.createAndSetInputs(builder, VanillaTypes.ITEM_STACK, Arrays.asList(List.of(image), paper), 0, 0);
        craftingGridHelper.createAndSetOutputs(builder, VanillaTypes.ITEM_STACK, List.of(out));
    }

}
