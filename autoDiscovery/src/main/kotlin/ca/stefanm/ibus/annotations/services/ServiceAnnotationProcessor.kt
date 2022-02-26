package ca.stefanm.ibus.annotations.services

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.lang.reflect.Type
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

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

        data class DiscoveredPlatformServiceGroup(
            val name : String,
            val description : String
        )
        data class DiscoveredServiceInfo(
            val name : String,
            val description : String
        )


        fileBuilder.addType(
            TypeSpec
                .classBuilder("DiscoveredServiceGroups")
                .addType(
                    TypeSpec.classBuilder("DiscoveredPlatformServiceGroup")
                        .addModifiers(KModifier.DATA)
                        .addProperty(PropertySpec.builder("name", String::class.asTypeName()).initializer("name").build())
                        .addProperty(PropertySpec.builder("description", String::class.asTypeName()).initializer("description").build())
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter(name = "name", type = String::class,)
                                .addParameter(name = "description", type = String::class, KModifier.PUBLIC)
                                .build()
                        )
                        .build()
                )
                .addType(
                    TypeSpec.classBuilder("DiscoveredServiceInfo")
                        .addModifiers(KModifier.DATA)
                        .addProperty(PropertySpec.builder("name", String::class.asTypeName()).initializer("name").build())
                        .addProperty(PropertySpec.builder("description", String::class.asTypeName()).initializer("description").build())
                        .primaryConstructor(
                            FunSpec.constructorBuilder()
                                .addParameter(name = "name", type = String::class,)
                                .addParameter(name = "description", type = String::class, KModifier.PUBLIC)
                                .build()
                        )
                        .build()
                )
                .addFunction(
                    FunSpec.builder("getAllGroups")
                        .returns(
                            SET.parameterizedBy(ClassName(
                                "ca.stefanm.ibus.car.platform",
                                "DiscoveredServiceGroups",
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
                            "DiscoveredServiceGroups",
                            "DiscoveredServiceInfo"
                            ))
                        )
                        .apply {
                            addCode(CodeBlock.builder()
                                .add("return setOf(\n")
                                .withIndent {
                                    services.map {
                                        it.getAnnotation(PlatformServiceInfo::class.java)
                                    }.map {
                                        add("DiscoveredServiceInfo(name= %S, description= %S), \n", it.name, it.description)
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
                                    "DiscoveredServiceGroups",
                                    "DiscoveredServiceInfo",
                                ),
                                LIST.parameterizedBy(ClassName("ca.stefanm.ibus.car.platform",
                                "DiscoveredServiceGroups",
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
                                    DiscoveredServiceInfo(
                                        name = service.getAnnotation(PlatformServiceInfo::class.java).name,
                                        description = service.getAnnotation(PlatformServiceInfo::class.java).description
                                    ),
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
                                        add("DiscoveredServiceInfo(name= %S, description= %S) to %L, \n", service.name, service.description, groupsFragment)
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
                                    "DiscoveredServiceGroups",
                                    "DiscoveredServiceInfo",
                                ),
                                LIST.parameterizedBy(ClassName("ca.stefanm.ibus.car.platform",
                                    "DiscoveredServiceGroups",
                                    "DiscoveredPlatformServiceGroup"
                                ))
                            )
                        )
                        .apply {
                            //TODO don't bother re-doing the work above, re-use the result and munch the map.
                        }
                        .build()
                )
                .build()
        )





        val file = fileBuilder.build()
        file.writeTo(System.out)
        file.writeTo(filer)
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