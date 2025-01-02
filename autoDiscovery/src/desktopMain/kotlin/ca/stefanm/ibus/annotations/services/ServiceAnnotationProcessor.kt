package ca.stefanm.ibus.annotations.services

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror

class ServiceAnnotationProcessor : AbstractProcessor() {

    override fun getSupportedAnnotationTypes(): MutableSet<String> = mutableSetOf(
        PlatformServiceGroup::class.java.canonicalName,
        PlatformServiceInfo::class.java.canonicalName
    )

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latest()

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
            generateSource(
                groups = env.getElementsAnnotatedWith(PlatformServiceGroup::class.java),
                services = env.getElementsAnnotatedWith(PlatformServiceInfo::class.java)
            )
        }
        return true
    }

    data class DiscoveredPlatformServiceGroup(
        val name : String,
        val description : String
    )
    data class DiscoveredServiceInfo(
        val name : String,
        val description : String,
        val implementingClass : TypeMirror
    )

    @Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    private fun generateSource(
        groups : Set<Element>,
        services : Set<Element>
    ) {


        val packageName = "ca.stefanm.ibus.car.platform"
        val fileBuilder = FileSpec.builder(
            packageName,
            "ServicesAndServiceGroups"
        )

        fileBuilder.addType(
            TypeSpec
                .classBuilder("DiscoveredServiceGroups")
                .addFunction(
                    FunSpec.builder("getAllGroups")
                        .returns(
                            SET.parameterizedBy(ClassName(
                                "ca.stefanm.ibus.car.platform",
                                "DiscoveredPlatformServiceGroup"
                            ))
                        )
                        .apply {


                            val discoveredGroups = groups.map {
                                it.getAnnotation(PlatformServiceGroup::class.java).let { ann ->
                                    DiscoveredPlatformServiceGroup(
                                        name = ann.name,
                                        description = ann.description
                                    )
                                }
                            }

                            val code = CodeBlock.builder()
                                .add("return setOf(\n")
                                .withIndent {
                                    discoveredGroups.map {
                                        add("DiscoveredPlatformServiceGroup(name= %S, description= %S), \n", it.name, it.description)
                                    }
                                }
                                .add(")")
                                .build()

                            this.addCode(code)
                        }.build()
                )
                .addFunction(
                    //This returns all discovered services
                    FunSpec.builder("getAllServiceInfos")
                        .addComment("Returns all services annotated with PlatformServiceInfo")
                        .returns(
                            SET.parameterizedBy(ClassName(
                            "ca.stefanm.ibus.car.platform",
                            "DiscoveredServiceInfo"
                            ))
                        )
                        .apply {
                            addCode(CodeBlock.builder()
                                .add("return setOf(\n")
                                .withIndent {
                                    services.map { service ->
                                        service.toDiscoveredServiceInfo()
                                    }.map { discoveredService ->
                                        discoveredService.toCode()(this)
                                    }
                                }.add(")").build()
                            )
                        }.build()
                )
                .addFunction(
                    FunSpec.builder("getServiceGroupsForService")
                        .addComment("Returns all discovered groups for a service")
                        .returns(
                            MAP.parameterizedBy(
                                ClassName("ca.stefanm.ibus.car.platform",
                                    "DiscoveredServiceInfo",
                                ),
                                LIST.parameterizedBy(ClassName("ca.stefanm.ibus.car.platform",
                                    "DiscoveredPlatformServiceGroup"
                                ))
                            )
                        )
                        .apply {

                            //services.first().annotationMirrors[2].annotationType.asElement().getAnnotation(PlatformServiceGroup::class.java).name
                            //Gets to "Peripherals"

                            //services.first().annotationMirrors.map { it.annotationType }.map { it.asElement() }[1].getAnnotation(PlatformServiceGroup::class.java)?.name
                            val data = services.map { service ->
                                Pair(
                                    service.toDiscoveredServiceInfo(),
                                    service
                                        .annotationMirrors
                                        .map { it.annotationType }
                                        .map { it.asElement() }
                                        .filter { it.getAnnotation(PlatformServiceGroup::class.java) != null }
                                        .map {
                                            DiscoveredPlatformServiceGroup(
                                                name = it.getAnnotation(PlatformServiceGroup::class.java).name,
                                                description = it.getAnnotation(PlatformServiceGroup::class.java).description
                                            )
                                        }
                                )
                            }.toMap()

                            addCode(CodeBlock.builder()
                                .add("return mapOf(\n")
                                .withIndent {
                                    for (service in data.keys) {
                                        val groupsFragment = if (data[service]!!.isEmpty()) {
                                            CodeBlock.builder().add("listOf()").build()
                                        } else {
                                            CodeBlock.builder()
                                                .add("listOf(")
                                                .withIndent {
                                                    data[service]!!.forEach {
                                                        add("DiscoveredPlatformServiceGroup(name= %S, description= %S), \n", it.name, it.description)
                                                    }
                                                }
                                                .add(")").build()
                                        }
                                        add("DiscoveredServiceInfo(name= %S, description= %S, implementingClass = %T::class, accessor = %L) to %L, \n",
                                            service.name,
                                            service.description,
                                            service.implementingClass.asTypeName(),
                                            CodeBlock.builder().add(" { discoveredService${service.name}() } ").build(),
                                            groupsFragment
                                        )
                                    }
                                }
                                .add(")")
                                .build()
                            )
                        }
                        .build()
                )
                .addFunction(
                    FunSpec.builder("getServiceInfosByGroup")
                        .addComment("Returns all discovered services, grouped by PlatformServiceGroup")
                        .returns(
                            MAP.parameterizedBy(
                                ClassName("ca.stefanm.ibus.car.platform",
                                    "DiscoveredPlatformServiceGroup"
                                ),
                                LIST.parameterizedBy(ClassName("ca.stefanm.ibus.car.platform",
                                    "DiscoveredServiceInfo",
                                ))
                            )
                        )
                        .apply {
                            //TODO don't bother re-doing the work above, re-use the result and munch the map.

                            val groupsByServices = services.map { service ->
                                service to service
                                    .annotationMirrors
                                    .map { it.annotationType }
                                    .map { it.asElement() }
                                    .filter { it.getAnnotation(PlatformServiceGroup::class.java) != null }
                                    .map {
                                        DiscoveredPlatformServiceGroup(
                                            name = it.getAnnotation(PlatformServiceGroup::class.java).name,
                                            description = it.getAnnotation(PlatformServiceGroup::class.java).description
                                        )
                                    }
                            }.toMap()
                                .mapKeys { it.key.toDiscoveredServiceInfo() }
                                .mapValues { tuple -> tuple.value.map { it to tuple.key } }
                                .entries
                                .map { it.value }
                                .flatten()
                                .groupBy { it.first }
                                .mapValues { it.value.map { it.second } }

                            addCode(CodeBlock.builder()
                                .add("return mapOf(")
                                .withIndent {
                                    for (group in groupsByServices.keys) {
                                        val servicesList = if (groupsByServices[group]!!.isEmpty()) {
                                            CodeBlock.builder().add("listOf()").build()
                                        } else {
                                            CodeBlock.builder()
                                                .add("listOf(")
                                                .withIndent innerBuilder@{
                                                    groupsByServices[group]!!.forEach {
                                                        it.toCode().invoke(this@innerBuilder)
                                                    }
                                                }
                                                .add(")").build()
                                        }
                                        add(
                                            "DiscoveredPlatformServiceGroup(name= %S, description= %S) to %L, \n",
                                            group.name,
                                            group.description,
                                            servicesList
                                        )
                                    }
                                }
                                .add(")")
                                .build()
                            )
                        }
                        .build()
                )
                .build()
        )


//        fileBuilder.addType(TypeSpec.classBuilder("ServiceHolder")
//            .addAnnotation(
//                AnnotationSpec.builder(ClassName("ca.stefanm.ibus.car.di", "ConfiguredCarScope")).build()
//            )
//            .primaryConstructor(FunSpec
//                .constructorBuilder()
//                .addAnnotation(AnnotationSpec.builder(ClassName("javax.inject", "Inject")).build())
//                .build()
//            )
//            .build()
//        )



        val file = fileBuilder.build()
        file.writeTo(System.out)
        file.writeTo(filer)
    }


    private fun Element.toDiscoveredServiceInfo() : DiscoveredServiceInfo {
        return DiscoveredServiceInfo(
            name = this.getAnnotation(PlatformServiceInfo::class.java).name,
            description = this.getAnnotation(PlatformServiceInfo::class.java).description,
            implementingClass = this.asType()
        )
    }

    private fun DiscoveredServiceInfo.toCode() : CodeBlock.Builder.() -> Unit {
        val discoveredService = this
        return {

            val accessorLambda = CodeBlock.builder().add(" { discoveredService${discoveredService.name}() } ").build()

            add("DiscoveredServiceInfo(name= %S, description= %S, implementingClass = %T::class, accessor = %L), \n",
                discoveredService.name,
                discoveredService.description,
                discoveredService.implementingClass.asTypeName(),
                accessorLambda
            )
        }
    }

}


//TODO Services without a group.
//services.map { service ->
//    Pair(
//        service,
//        service
//            .annotationMirrors
//            .map { it.annotationType }
//            .map { it.asElement() }
//            .filter { it.getAnnotation(PlatformServiceGroup::class.java) == null }
//    )
//}