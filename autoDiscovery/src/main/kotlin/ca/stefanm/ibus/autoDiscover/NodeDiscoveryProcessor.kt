package ca.stefanm.ibus.autoDiscover

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.annotation.processing.*
import javax.inject.Named
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

class NodeDiscoveryProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
            AutoDiscover::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latest()
    }

    private val filer: Filer
        get() = processingEnv.filer
    private val messager : Messager
        get() = processingEnv.messager
    private val outputDirectory : String?
        get() = processingEnv.options["kapt.kotlin.generated"]

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
    }

    override fun process(p0: MutableSet<out TypeElement>, env: RoundEnvironment): Boolean {
        if (p0.isNotEmpty()) {
            generateSource(env.getElementsAnnotatedWith(p0.first()))
        }
        return true
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun generateSource(elements : Set<Element>) {

        val file = FileSpec.builder(
            "ca.stefanm.ibus.gui.menu.navigator",
            "AutoDiscoveredNodes"
        ).addType(
            TypeSpec.classBuilder("AutoDiscoveredNodesModule")
                .addAnnotation(Module::class)
                .addFunction(
                    FunSpec.builder("provideDiscoveredNodes")
                        .addAnnotation(Provides::class)
                        .addAnnotation(ElementsIntoSet::class)
                        .addAnnotation(
                            AnnotationSpec.builder(Named::class)
                                .addMember("\"all_nodes\"")
                                .build()
                        )
                        .returns(
                            ClassName("kotlin.collections", "Set")
                                .parameterizedBy(
                                    ClassName("ca.stefanm.ibus.gui.menu.navigator", "NavigationNode")
                                        .parameterizedBy(STAR)
                                )
                        )
                        .apply {
                            elements.forEachIndexed { index, element ->

                                val elementName = (element.asType().asTypeName() as ClassName).simpleName

                                addParameter(ParameterSpec.builder(
                                    elementName.lowercase(),
                                    element.asType().asTypeName()
                                ).build())
                            }
                        }
                        .apply {
                            val code = CodeBlock.builder()
                                .add("return setOf(\n")
                                .withIndent {
                                    elements.map {
                                        add((it.asType().asTypeName() as ClassName).simpleName.lowercase() + ",\n")
                                    }
                                }
                                .add(")")
                                .build()
                            addStatement(code.toString())
                        }
                        .build()
                ).build()
        ).build()

        file.writeTo(System.out)
        file.writeTo(filer)
    }
}