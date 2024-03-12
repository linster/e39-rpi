package ca.stefanm.ibus.annotations.screendoc

import com.squareup.kotlinpoet.ClassName

data class RawLink(
    val from : ClassName,
    val to : ClassName?
)

data class GraphPartition2(
    val partitionName : String,
    val links : List<RawLink>
)