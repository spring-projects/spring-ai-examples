package com.example.rag_with_kotlin

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor
import org.springframework.ai.chat.client.entity
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.support.beans
import org.springframework.data.annotation.Id
import org.springframework.data.repository.ListCrudRepository
import org.springframework.web.servlet.function.ServerResponse
import org.springframework.web.servlet.function.router


/**
 * @author Josh Long
 */
@SpringBootApplication
class RagWithKotlinApplication

fun main(args: Array<String>) {
    runApplication<RagWithKotlinApplication>(*args) {
        val context = beans {
            bean {
                val repo = ref<DogRepository>()
                val vs = ref<VectorStore>()
                repo.findAll().forEach {
                    println("got a dog $it")
                    val dogument = Document("id: ${it.id}, name: ${it.name}, description: ${it.description}")
                    vs.add(listOf(dogument))
                }
                val system = """
                    You are an AI powered assistant to help people adopt a dog from the adoption 
                    agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris, 
                    Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available 
                    will be presented below. If there is no information, then return a polite response suggesting we 
                    don't have any dogs available.
                """.trimIndent()
                ref<ChatClient.Builder>()
                    .defaultSystem(system)
                    .defaultAdvisors(QuestionAnswerAdvisor(ref<VectorStore>()))
                    .build()
            }
            bean {
                router {
                    GET("/dogs") { ServerResponse.ok().body(ref<DogRepository>().findAll()) }
                }
            }
            bean {
                val cc = ref<ChatClient>()
                cc
                    .prompt(" do you have any neurotic dogs? ")
                    .call()
                    .entity<DogAdoptionSuggestion>().also {
                        print(it)
                    }
            }
        }
        addInitializers(context)
    }
}

data class DogAdoptionSuggestion(val name: String, val id: Int, val description: String)

interface DogRepository : ListCrudRepository<Dog, Int>

data class Dog(@Id val id: Int, val name: String, val owner: String?, val description: String)
