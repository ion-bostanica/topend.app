workspace "TopEnd" "Open source personal fitness app — learning-in-public" {

    model {
        user = person "User" "Logs meals and training; here: posts a photo of what they ate"

        mobileApp = softwareSystem "TopEnd Mobile App" "Future client — descoped for now" {
            tags "Future"
        }

        topend = softwareSystem "TopEnd" "Personal fitness platform" {
            api = container "API" "Spring Boot 4 / WebFlux, hexagonal per-feature modules" "Java 26, Spring AI 2" {
                controller = component "NutritionController" "POST /nutrition/consumptions (multipart photo | JSON)"
                service = component "ConsumptionService" "Use case orchestration; in-memory store for now"
                extraction = component "Extraction adapters" "Anthropic / OpenAI vision + tool-calling agent"
                foodTool = component "FoodSearchTool" "@Tool the model calls to resolve foods"
                fdcClient = component "FdcClient" "WebClient consumer of USDA FDC search API"
            }
            db = container "MongoDB" "Planned — data model approved in DBML, persistence not wired yet" "mongo:8" {
                tags "Planned"
            }
        }

        anthropic = softwareSystem "Anthropic API" "Claude vision + tool use" {
            tags "External"
        }
        openai = softwareSystem "OpenAI API" "GPT vision + tool use" {
            tags "External"
        }
        fdc = softwareSystem "USDA FoodData Central" "Authoritative food & nutrient database (free API)" {
            tags "External"
        }

        user -> topend "Posts meal photo / confirmed consumption" "HTTPS"
        mobileApp -> topend "Will call" "HTTPS"
        topend -> anthropic "Vision extraction + tool-use loop" "HTTPS"
        topend -> openai "Vision extraction + tool-use loop" "HTTPS"
        topend -> fdc "GET /v1/foods/search" "HTTPS"

        user -> api "POST /nutrition/consumptions"
        api -> anthropic "chat w/ image + tools"
        api -> openai "chat w/ image + tools"
        api -> fdc "food search"
        api -> db "will persist consumptions"

        controller -> service "delegates"
        service -> extraction "VisionExtractionPort"
        extraction -> foodTool "model invokes"
        foodTool -> fdcClient "delegates"
    }

    views {
        systemContext topend "SystemContext" {
            include *
            autolayout lr
        }
        container topend "Containers" {
            include *
            autolayout lr
        }
        styles {
            element "Person" {
                shape person
                background #1168bd
                color #ffffff
            }
            element "Software System" {
                background #1168bd
                color #ffffff
            }
            element "External" {
                background #999999
                color #ffffff
            }
            element "Future" {
                background #cccccc
                color #333333
                border dashed
            }
            element "Planned" {
                background #cccccc
                color #333333
                border dashed
            }
            element "Container" {
                background #438dd5
                color #ffffff
            }
        }
    }
}
