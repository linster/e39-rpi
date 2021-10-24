package ca.stefanm.ibus.autoDiscover

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.*
import javax.lang.model.element.Element

class DiscoverySymbolProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return DiscoverySymbolProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger
        )
    }
}

class DiscoverySymbolProcessor(
    val codeGenerator: CodeGenerator,
    val logger : KSPLogger
) : SymbolProcessor {

    private companion object {
        object Output {
            const val packageName = "ca.stefanm.ibus.di"
            const val className = "AutoDiscoveredNodesRegistry"
        }
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        val symbols = resolver.getSymbolsWithAnnotation(AutoDiscover::class.qualifiedName!!)
        symbols.map { it. }
    }

    inner class DiscoveryVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            super.visitClassDeclaration(classDeclaration, data)


            val outputStream = codeGenerator.createNewFile(
                Dependencies(true, classDeclaration.containingFile!!),
                packageName = Output.packageName,
                fileName = Output.className
            )
        }
    }

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun generateSource(elements : Set<Element>) {

        val file = FileSpec.builder(Output.packageName, Output.className
        ).addType(
            TypeSpec.classBuilder(Output.className)
                .primaryConstructor(
                    FunSpec.constructorBuilder()
                        //.addAnnotation(Inject::class.java)
                        .build()
                )
                .addFunction(
                    FunSpec.builder("getAllDiscoveredNodeClasses")
                        .apply {
                            val code = CodeBlock.builder()
                                .add("return setOf(\n")
                                .withIndent {
                                    elements.map {
                                        add("%T::class.java,\n", (it.asType().asTypeName() as ClassName))
                                    }
                                }
                                .add(")")
                                .build()
                            addStatement(code.toString())
                        }
                        .build()
                )
                .build()
        ).build()

        file.writeTo(System.out)
        file.writeTo(filer)

    }

}