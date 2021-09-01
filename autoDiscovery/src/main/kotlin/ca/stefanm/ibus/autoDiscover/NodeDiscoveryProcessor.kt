package ca.stefanm.ibus.autoDiscover

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dagger.Module
import dagger.Provides
import dagger.multibindings.ElementsIntoSet
import javax.annotation.processing.*
import javax.inject.Inject
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

        val constructor = FunSpec.constructorBuilder()
            .addAnnotation(Inject::class)
            .apply {

                addParameter(
                    "autoDiscoveredNodesHolder",
                    ClassName("ca.stefanm.ibus.di", "AutoDiscoveredNodesHolder"),
                    KModifier.PRIVATE
                )

                elements.forEach {
                    addParameter(
                        (it.asType().asTypeName() as ClassName).simpleName.lowercase(),
                        it.asType().asTypeName(),
                        KModifier.PRIVATE
                    )
                }
            }.build()

        val file = FileSpec.builder(
            "ca.stefanm.ibus.di",
            "AutoDiscoveredNodesDiscoverer"
        ).addType(
            TypeSpec.classBuilder("AutoDiscoveredNodeDiscoverer")
                .primaryConstructor(constructor)
                .apply {

                    addProperty(
                        PropertySpec.builder(
                            "autoDiscoveredNodesHolder",
                            ClassName("ca.stefanm.ibus.di", "AutoDiscoveredNodesHolder")
                        ).initializer("autoDiscoveredNodesHolder")
                            .addModifiers(KModifier.PRIVATE)
                            .build()
                    )

                    elements.forEach {
                        addProperty(
                            PropertySpec.builder(
                                (it.asType().asTypeName() as ClassName).simpleName.lowercase(),
                                it.asType().asTypeName(),
                            ).initializer(
                                (it.asType().asTypeName() as ClassName).simpleName.lowercase(),
                            ).addModifiers(KModifier.PRIVATE)
                            .build()
                        )
                    }
                }
                .addFunction(
                    FunSpec.builder("getAllNodes")
                        //.addModifiers(KModifier.OVERRIDE)
                        .returns(
                            ClassName("kotlin.collections", "Set")
                                .parameterizedBy(
                                    ClassName("ca.stefanm.ibus.gui.menu.navigator", "NavigationNode")
                                        .parameterizedBy(STAR)
                                )
                        )
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
                ).addInitializerBlock(CodeBlock.of(
                    "autoDiscoveredNodesHolder.autoDiscoveredNodes.addAll(getAllNodes()) \n"
                ))
                .build()
        ).build()

        file.writeTo(System.out)


        //TODO don't write this using the filer to make a source
        //TODO file. instead, write to a random file.
        //TODO, then make a gradle task that copies that random file.
        //TODO the gradle task can be something like "AutoDiscoverScreens"
        //TODO. This task must be run once, which will run kapt to process
        //TODO all the annotations, then it will create the file we need and
        //TODO place it in the DI folder. Then we can run the kapt again.
        //TODO we can get fancy by making a stub class of this where the getNodes()
        //TODO doesn't return anything, but this will allow us to run dagger to
        //TODO generate the factories. Then the AutoDiscover gradle task can be used
        //TODO to overwrite that stub file.
        file.writeTo(filer)

    }
}