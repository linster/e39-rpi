package ca.stefanm.ibus.annotations.screenflow

import javax.inject.Qualifier
import kotlin.reflect.KClass

@Qualifier
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenDoc(
    val screenName : String,
    val description : String,
) {

    /** Use this to provide hints for graph slices to make */
    /** Same idea at PlatformServiceGroup, make annotations for each partition */
    @Qualifier
    @Target(AnnotationTarget.ANNOTATION_CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class GraphPartition(
        val partitionName : String,
        val description : String
    )

    @Qualifier
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class NavigateTo(
        /** Use a KClass of a NavigationNode, not an annotation */
        val targetClass : KClass<*>,
        val linkDescription : String,
        val targetDescription : String = ""
    )

    /** Put this on screens that have a "Go Back" option so that it can be noted
     * that there are no dead ends in the navigation graph.
     */
    @Qualifier
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AllowsGoBack

    @Target(AnnotationTarget.CLASS)
    @Repeatable
    annotation class OpensSubScreen(
        val subscreenName : String
    )

    /** Use this to document panes that can pop out on a screen */
    @Target(AnnotationTarget.FUNCTION)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class SubScreen(
        /** A Subscreen name, unique to a screen */
        val screenName : String,
        val paneDescription : String
    ) {

        /** Allows closing the parent screen */
        @Target(AnnotationTarget.FUNCTION)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class AllowsCloseParent

        @Target(AnnotationTarget.FUNCTION)
        @Retention(AnnotationRetention.RUNTIME)
        @Repeatable
        annotation class NavigateToSubscreen(
            val screenName : String
        )


    }
}