package sh.adelessfox.odradek.ui.actions;

public abstract class Action {
    public void perform(ActionContext context) {
        // do nothing by default
    }

    public boolean isEnabled(ActionContext context) {
        return isVisible(context);
    }

    public boolean isVisible(ActionContext context) {
        return true;
    }

    public interface Check {
        boolean isChecked(ActionContext context);
    }

    public interface Radio {
        boolean isSelected(ActionContext context);
    }
}
