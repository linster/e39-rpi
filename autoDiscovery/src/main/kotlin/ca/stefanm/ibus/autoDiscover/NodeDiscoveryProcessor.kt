package ca.stefanm.ibus.autoDiscover

import com.squareup.kotlinpoet.*
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

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

    override fun process(p0: MutableSet<out TypeElement>?, p1: RoundEnvironment?): Boolean {
        println("BOB WAT ANNOTATION")

//        messager.printMessage(Diagnostic.Kind.ERROR, "BOB WAT WAS HERE")
//        messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "BOB WAT WARNED YOU")


        return true
    }
}