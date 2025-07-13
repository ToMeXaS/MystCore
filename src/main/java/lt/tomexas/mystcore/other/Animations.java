package lt.tomexas.mystcore.other;

import com.ticxo.modelengine.api.ModelEngineAPI;
import com.ticxo.modelengine.api.animation.property.IAnimationProperty;
import lombok.Getter;
import lt.tomexas.mystcore.submodules.resources.trees.data.Tree;

public class Animations {

    @Getter
    public enum ANIMATION_LIST {
        FALL("fall");

        private final String animationName;

        ANIMATION_LIST(String animationName) {
            this.animationName = animationName;
        }

    }

    public static IAnimationProperty play(Tree tree, ANIMATION_LIST animation) {
        return ModelEngineAPI.getModeledEntity(tree.getUuid())
                .getModel(tree.getModelId())
                .orElseThrow(() -> new IllegalStateException("Model not found!"))
                .getAnimationHandler()
                .playAnimation(animation.getAnimationName(), 0.3, 0.3, 1, true);
    }

}
