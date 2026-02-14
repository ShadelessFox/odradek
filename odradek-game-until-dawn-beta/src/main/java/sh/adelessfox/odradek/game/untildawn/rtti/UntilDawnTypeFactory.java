package sh.adelessfox.odradek.game.untildawn.rtti;

import sh.adelessfox.odradek.rtti.TypeInfo;
import sh.adelessfox.odradek.rtti.factory.AbstractTypeFactory;
import sh.adelessfox.odradek.rtti.factory.TypeId;

import java.lang.invoke.MethodHandles;
import java.util.List;

public class UntilDawnTypeFactory extends AbstractTypeFactory {
    public UntilDawnTypeFactory() {
        super(UntilDawn.class, MethodHandles.lookup());
    }

    @Override
    protected TypeId computeTypeId(TypeInfo info) {
        return new UntilDawnTypeId(info.name());
    }

    @Override
    protected void sortOrderedAttributes(List<OrderedAttr> attrs) {
        // Attributes are not sorted
    }

    @Override
    protected void filterOrderedAttributes(List<OrderedAttr> attrs) {
        attrs.removeIf(attr -> !attr.attr().isSerialized());
    }
}
