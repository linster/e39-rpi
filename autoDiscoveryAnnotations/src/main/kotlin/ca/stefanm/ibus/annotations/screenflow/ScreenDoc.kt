package ca.stefanm.ibus.annotations.screenflow

import javax.inject.Qualifier
import kotlin.reflect.KClass

@Qualifier
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ScreenDoc(
    val screenName : String,
    val description : String,
    //These can't be a repeatable annotation for the class.
    val navigatesTo : Array<NavigateTo> = []
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

    annotation class NavigateTo(
        /** Use a KClass of a NavigationNode, not an annotation */
        val targetClass : KClass<*>,
        val linkDescription : String = "",
        val targetDescription : String = ""
    )

    /** Put this on screens that have a "Go Back" option so that it can be noted
     * that there are no dead ends in the navigation graph.
     */
    @Qualifier
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    @Repeatable
    annotation class AllowsGoBack(
        val extraEdgeDescriptions : Array<Description> = [] //Empty list is just a back-stack go back edge, no extra edges with descriptions
    ) {
        annotation class Description(val description : String)
    }

    @Qualifier
    @Target(AnnotationTarget.CLASS)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class AllowsGoRoot

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
        val paneDescription : String,
        /** Set to true if there are multiple, or they are dynamically generated by code */
        val isDynamic : Boolean = false,
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

        /** Used for Subscreen Classes that are
         *  not just composable functions of
         *  subscreens.
         */
        @Target(AnnotationTarget.CLASS)
        @Retention(AnnotationRetention.RUNTIME)
        annotation class SetParent(
            val subscreenHolder : KClass<*>,
        )


    }
}