package sh.adelessfox.odradek.rtti.generator;

import sh.adelessfox.odradek.rtti.GenerateBindings;
import sh.adelessfox.odradek.rtti.runtime.TypeContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.net.URL;
import java.util.Set;
import java.util.function.Supplier;

public class GenerateBindingsProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var filer = processingEnv.getFiler();
        var messager = processingEnv.getMessager();

        for (Element element : roundEnv.getElementsAnnotatedWith(GenerateBindings.class)) {
            var module = (ModuleElement) element;
            var annotation = module.getAnnotation(GenerateBindings.class);

            var context = new TypeContext();
            try {
                context.load(getResourceUrl(annotation.input().types()), getResourceUrl(annotation.input().extensions()));
            } catch (IOException e) {
                messager.printError("An error occurred while reading type definitions: " + e.getMessage(), module);
            }

            var targetDesc = ClassDesc.of(annotation.target());
            var targetPackage = targetDesc.packageName();
            var targetClass = targetDesc.displayName();

            var generator = new TypeSourceGenerator(targetPackage, targetClass);
            for (GenerateBindings.Builtin builtin : annotation.builtins()) {
                generator.addBuiltin(builtin.type(), asTypeMirror(builtin::repr));
            }
            for (GenerateBindings.Callback callback : annotation.callbacks()) {
                var type = asTypeMirror(callback::handler);
                generator.addCallback(callback.type(), type);
            }
            for (GenerateBindings.Extension extension : annotation.extensions()) {
                var type = asTypeMirror(extension::extension);
                generator.addExtension(extension.type(), type);
            }
            generator.addAll(context.getAll());

            try {
                generator.build().writeTo(filer);
            } catch (IOException e) {
                messager.printError("Am error occurred while writing generated code: " + e.getMessage());
            }
        }

        return true;
    }

    private URL getResourceUrl(String name) throws IOException {
        return processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", name).toUri().toURL();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(GenerateBindings.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private TypeMirror asTypeMirror(Supplier<Class<?>> supplier) {
        try {
            supplier.get();
            throw new AssertionError();
        } catch (MirroredTypeException e) {
            return e.getTypeMirror();
        }
    }
}
