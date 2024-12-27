package ca.stefanm.ibus.annotations.screendoc

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.type.TypeMirror


//This file contains the book-keeping information
//the annotation processor uses to build up a nav graph.

//It does not contain code that is generated... for that, we
//need to actually use KotlinPoet to generate some classes that
//we can import in the app.

data class GraphPartition(
    val name : String,
    val description: String
)

data class Screen(
    val name : String,
    val description : String,
    val isRoot : Boolean = false,

    val screenLinks : MutableSet<Link>,

    //TODO add a className here for easier lookup.
    val typeMirror: ClassName
) {

    sealed interface Link {
        val targetScreen : Screen
        data class ToLink(
            override val targetScreen : Screen,
            val linkDescription : String = "",
            val targetDescription : String = ""
        ) : Link

        data class AllowsGoBackLink(
            override val targetScreen: Screen,
            val optionalTag : String,
        ) : Link

        data class AllowsGoRootLink(
            override val targetScreen: Screen
        ) : Link
    }

    data class SubScreen(
        val name : String,
        val paneDescription : String,
        val isDynamic : Boolean = false,

        val parentScreen : Screen,

        val links : MutableSet<Link>
    ) {

        sealed interface Link {
            data class ToScreen(val screen : Screen)
            data class ToSubScreen(val subScreen : SubScreen)
        }
    }
}

//TODO we need a screen tree for a graph partition

//TODO we need a class that has all graph partitions, and a Map<Partition, ScreenTree>

data class ScreenTree(
    val rootScreen : Screen,
    /** The set of all screens processed */
    val allScreens : Set<Screen>,
    /** Screens not reachable from the rootScreen */
    val disconnectedScreens : Set<Screen>
)

data class DiscoveredGraphPartitions(
    /** The set of all discovered graph partitions */
    val allPartitions : Set<GraphPartition>,

    /** Key is the requested partition. Value is the set of all partitions
     *  the requested partition belongs to */
    val parentsForPartition : Map<GraphPartition, Set<GraphPartition>>,

    /** Key is the requested partition. Value is the set of all partitions
     *  who are children of this partition.
     */
    val childrenForPartition : Map<GraphPartition, Set<GraphPartition>>
)

data class DiscoveredGlobalScreenTree(
    val screenTreeByPartition : Map<GraphPartition, ScreenTree>,
)