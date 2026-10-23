package Utils

object Queries {
    val json = """
                {
                    "first_field": "abc"
                }
                """.trimIndent()

    val json1 = """
                {
                    "first_field": "abc",
                    "second_field": "custom"
                }
                """.trimIndent()

    val json2 = """
                {
                    "first_field": "custom"
                }
                """.trimIndent()

    val json3 = """
                {
                    "second_field": "xyz"
                }                
                """.trimIndent()

    val json4 = """
                {
                }                
                """.trimIndent()

    val json5 = """
                {
                    "third_field": 10
                }                
                """.trimIndent()

    val json6 = """
                {
                    "third_field": 20
                }                
                """.trimIndent()
    val json7 = """
                {
                    "third_field": 30
                }                
                """.trimIndent()
    val json8 = """
                {
                    "first_field": "abc",
                    "third_field": 30
                }                
                """.trimIndent()
    val json9 = """
                {
                    "first_field": "custom",
                    "second_field": "xyz",
                    "third_field": 80
                }
                """.trimIndent()


    // Only for searching
    val query10 = """
                {
                    "first_field": {"${'$'}eq": "abc"}
                }
                """.trimIndent()

    val query11 = """
                {
                    "third_field": {"${'$'}gt": 20}
                }
                """.trimIndent()

    val query12 = """
                {
                    "first_field": "abc",
                    "third_field": {"${'$'}gt": 20}
                }
                """.trimIndent()
    val query13 = """
                {
                    "${'$'}and": [
                        { "first_field": "abc" },
                        { "third_field": {"${'$'}gt": 20} }
                    ]
                }
                """.trimIndent()

    val query14 = """
                {
                    "${'$'}or": [
                        { "first_field": "abc" },
                        { "third_field": {"${'$'}gt": 20} }
                    ]
                }
                """.trimIndent()

    val query15 = """
                {
                    "first_field": {"${'$'}ne": "abc"}
                }
                """.trimIndent()
}
