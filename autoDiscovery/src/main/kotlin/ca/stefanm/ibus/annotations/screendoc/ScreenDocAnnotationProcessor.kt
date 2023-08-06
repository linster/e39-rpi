package ca.stefanm.ibus.annotations.screendoc

import ca.stefanm.ibus.annotations.screenflow.ScreenDoc
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.Messager
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException

class ScreenDocAnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(
//            ScreenDoc::class.java.canonicalName,
//            ScreenDoc.GraphPartition::class.java.canonicalName,
            ScreenDoc.NavigateTo::class.java.canonicalName,
//            ScreenDoc.AllowsGoBack::class.java.canonicalName,
//            ScreenDoc.AllowsGoRoot::class.java.canonicalName,
//            ScreenDoc.OpensSubScreen::class.java.canonicalName,

//            ScreenDoc.SubScreen::class.java.canonicalName,
//            ScreenDoc.SubScreen.AllowsCloseParent::class.java.canonicalName,
//            ScreenDoc.SubScreen.NavigateToSubscreen::class.java.canonicalName,
//            ScreenDoc.SubScreen.SetParent::class.java.canonicalName
        )
    }

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

    private val filer: Filer
        get() = processingEnv.filer
    private val messager : Messager
        get() = processingEnv.messager
    private val outputDirectory : String?
        get() = processingEnv.options["kapt.kotlin.generated"]


    override fun process(annotations: MutableSet<out TypeElement>,
                         env: RoundEnvironment): Boolean {

        if (annotations.isNotEmpty()) {
            val screenDocs = env.getElementsAnnotatedWith(ScreenDoc::class.java)

            val screenList = generateScreenList(env)
            addDirectScreenLinks(screenList, env)

//            addDirectScreenLinks(emptyMap(), env)

        }
        return true
    }

    private fun generateScreenList(
        env: RoundEnvironment
    ) : Map<Element, Screen> {

        val screenDocs = env.getElementsAnnotatedWith(ScreenDoc::class.java)

        return screenDocs.associateWith {
            val screenDocAnnotation = it.getAnnotation(ScreenDoc::class.java) as ScreenDoc

            val modelScreen = Screen(
                name = screenDocAnnotation.screenName,
                description = screenDocAnnotation.description,
                isRoot = false,
                screenLinks = mutableSetOf(),
                typeMirror = (it.asType().asTypeName() as ClassName)
            )

            modelScreen
        }
    }

    private fun ScreenDoc.NavigateTo.toNaiveModel(from : ClassName) : RawLink {
        return RawLink(
            from = from,
            to = try {
                this.targetClass.asClassName()
            } catch (e : Throwable) {
                //TODO try and get a ClassName out of the failure to read TargetClass.
                null
            }
        )
    }

    private fun addDirectScreenLinks(
        upstream : Map<Element, Screen>,
        env : RoundEnvironment
    ) : Map<Element, Screen> {

        //First, every NavigateTo needs to have it's KClass<*> be changed to
        //a TypeMirror or element?


        val upstreamCopy = upstream.toMutableMap()
        val links = upstream.keys
            .map { it to it.getAnnotationsByType(ScreenDoc.NavigateTo::class.java) }
            .filter { it.second.isNotEmpty() }





        val navToLinks = env.getElementsAnnotatedWith(ScreenDoc.NavigateTo::class.java)

        print(navToLinks)

        return upstreamCopy
    }
}