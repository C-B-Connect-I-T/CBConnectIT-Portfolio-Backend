package com.cbconnectit.plugins

import com.cbconnectit.data.database.tables.*
import com.cbconnectit.domain.models.company.Company
import com.cbconnectit.domain.models.experience.Experience
import com.cbconnectit.domain.models.jobPosition.JobPosition
import com.cbconnectit.domain.models.link.Link
import com.cbconnectit.domain.models.link.LinkType
import com.cbconnectit.domain.models.project.Project
import com.cbconnectit.domain.models.service.Service
import com.cbconnectit.domain.models.tag.Tag
import com.cbconnectit.domain.models.testimonial.Testimonial
import com.cbconnectit.domain.models.user.UserRoles
import com.cbconnectit.utils.PasswordManagerContract
import com.github.slugify.Slugify
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import java.time.LocalDateTime
import java.util.*

fun Application.configureDatabase() {
    val passwordEncryption by inject<PasswordManagerContract>()

    Database.connect(
        System.getenv("database-url"),
        user = System.getenv("database-username"),
        password = System.getenv("database-password")
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(
            CompaniesLinksPivotTable,
            CompaniesTable,
            ExperiencesTable,
            JobPositionsTable,
            LinksProjectsPivotTable,
            LinksTable,
            ProjectsTable,
            ServicesTable,
            TagsExperiencesPivotTable,
            TagsProjectsPivotTable,
            TagsTable,
            TestimonialsTable,
            UsersTable
        )

        seedDatabase(passwordEncryption)
    }
}

private fun seedDatabase(passwordEncryption: PasswordManagerContract) {

    UsersTable.insertIgnore {
        it[fullName] = "Christiano Bolla"
        it[username] = "bollachristiano@gmail.com"
        it[password] = passwordEncryption.encryptPassword(System.getenv("ADMIN_SEED_PASSWORD"))
        it[role] = UserRoles.Admin
    }

    // Tags
    listOf(
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Library"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Coroutines"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Kotlin"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), name = "XML"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000005"), name = "MVVM"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000006"), name = "Jetpack Compose"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000007"), name = "Koin"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000008"), name = "Ktor"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009"), name = "Android"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000010"), name = "iOS"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000011"), name = "KMP"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000012"), name = "Flutter"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000013"), name = "Kobweb"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000014"), name = "Android TV"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000016"), name = "NodeJS"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000017"), name = "Javascript"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000018"), name = "MongoDB"),
        Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000019"), name = "Mongoose"),
    ).forEach { tag ->
        TagsTable.insertIgnore {
            it[id] = tag.id
            it[name] = tag.name
            it[slug] = Slugify.builder().build().slugify(tag.name)
        }
    }

    // Link
    listOf(
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), url = "https://zappware.com"),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), url = "https://wisemen.digital"),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), url = "https://www.cbconnectit.com"),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), url = "https://github.com/wisemen-digital/AndroidCore", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000005"), url = "https://github.com/ShaHar91/Measurements", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000006"), url = "https://github.com/ShaHar91/DemoPokedex", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000007"), url = "https://play.google.com/store/apps/details?id=be.christiano.demoPokedex", type = LinkType.PlayStore),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000008"), url = "https://github.com/ShaHar91/FoodWatcher-Android", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000009"), url = "https://github.com/ShaHar91/PoemCollection-backend-ktor", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000010"), url = "https://github.com/ShaHar91/PoemCollection-Backend-Node", type = LinkType.Github),
        Link(id = UUID.fromString("00000000-0000-0000-0000-000000000011"), url = "https://github.com/ShaHar91/PoemCollection-Android", type = LinkType.Github),
    ).forEach { link ->
        LinksTable.insertIgnore {
            it[id] = link.id
            it[url] = link.url
            it[type] = link.type
        }
    }

    // Companies
    listOf(
        Company(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "C.B. Connect I.T.", listOf(Link(id = UUID.fromString("00000000-0000-0000-0000-000000000003")))),
        Company(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Wisemen (formerly Appwise)", listOf(Link(id = UUID.fromString("00000000-0000-0000-0000-000000000002")))),
        Company(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Zappware", listOf(Link(id = UUID.fromString("00000000-0000-0000-0000-000000000001")))),
    ).forEach { company ->
        CompaniesTable.insertIgnore {
            it[id] = company.id
            it[name] = company.name
        }

        company.links.forEach { link ->
            CompaniesLinksPivotTable.insertIgnore {
                it[companyId] = company.id
                it[linkId] = link.id
            }
        }
    }

    // JobPositions
    listOf(
        JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000001"), name = "Android Lead"),
        JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000002"), name = "Customer/Testing Engineer"),
        JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000003"), name = "Android (TV) Developer"),
        JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000004"), name = "Developer"),
        JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000005"), name = "Android Developer"),
    ).forEach { company ->
        JobPositionsTable.insertIgnore {
            it[id] = company.id
            it[name] = company.name
        }
    }

    // Experiences
    listOf(
        Experience(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            shortDescription = "Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            description = "I collaborated on a project centered around enabling users to connect to an EV charger through Bluetooth, after which they could modify various settings. Operating within a Scrum framework, I contributed to delivering iterative updates, incorporating both new features and addressing bugs. My involvement extended to create a better connection experience and adding solar support.",
            from = LocalDateTime.of(2024, 1, 14, 0, 0),
            to = LocalDateTime.of(2024, 4, 16, 0, 0),
            asFreelance = true,
            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            )
        ),
        Experience(
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            shortDescription = "Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            description = "Engaged in the creation and management of more than 15 projects. I prioritised efficiency and consistency by developing and maintaining a Core library utilised across all newer projects, increasing code reusability and consistency of the code structure. Embracing the MVVM pattern as a personal preference, I focussed on implementing best practices and used official Android libraries to optimize the framework’s capabilities. Leveraging Google Maps API alongside FCM/OneSignal, I increased application functionality and user engagement, while also integrating the Material design libraries to deliver visually stunning apps. To facilitate seamless data communication, I integrated third-party libraries for processing RESTful API, the bulk of it being handled in the Core library. Additionally, I ensured a fluid user experience by working with an internal Room database, enabling the application to launch with cached data and maintaining a clear distinction between remote and local data storage. Furthermore, I also got to work on multiple projects where I established Bluetooth connections to EV-chargers, after which the user could modify various settings.",
            from = LocalDateTime.of(2017, 5, 1, 0, 0),
            to = LocalDateTime.of(2023, 10, 16, 0, 0),
            asFreelance = false,
            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            )
        ),
        Experience(
            id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
            shortDescription = "Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            description = "As an Android Developer I helped the team with the development and maintenance of Android and Android TV apps, collectively with well over 300k downloads. Working within a Scrum framework, I made sure the app had incremental updates, introducing new features and addressing bugs to continuously enhance user experience. The application itself was a multi white-labelled product, leveraging GraphQL technology to optimize data retrieval from databases, thereby reducing unnecessary network requests and ensuring efficient data fetching. Furthermore, I also got to work with MQTT for custom notifications, empowering real-time updates and personalised alerts to augment user engagement and interaction. ",
            from = LocalDateTime.of(2018, 1, 1, 0, 0),
            to = LocalDateTime.of(2020, 12, 31, 0, 0),
            asFreelance = false,
            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000014")),
            )
        ),
    ).forEach { experience ->
        ExperiencesTable.insertIgnore {
            it[id] = experience.id
            it[shortDescription] = experience.shortDescription
            it[description] = experience.description
            it[from] = experience.from
            it[to] = experience.to
            it[asFreelance] = experience.asFreelance
            it[companyId] = experience.company.id
            it[jobPositionId] = experience.jobPosition.id
        }

        experience.tags.forEach { tag ->
            TagsExperiencesPivotTable.insertIgnore {
                it[experienceId] = experience.id
                it[tagId] = tag.id
            }
        }
    }

    // Testimonials
    listOf(
//        Testimonial(
//            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
//            imageUrl = "https://raw.githubusercontent.com/ShaHar91/Portfolio/master/public/avatar1.png",
//            fullName = "Shrek",
//            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
//            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000001")),
//            review = "Working alongside Christiano has been an absolute privilege. Their tireless dedication to our team's growth and success is truly commendable. Christiano consistently goes above and beyond, readily offering assistance and putting in the effort to ensure our collective progress. As a coworker, their commitment to collaboration and their unwavering support make them an invaluable asset. Grateful to be part of a team led by someone who leads not just by words, but by inspiring actions."
//        ),
//        Testimonial(
//            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
//            imageUrl = "https://raw.githubusercontent.com/ShaHar91/Portfolio/master/public/avatar2.png",
//            fullName = "Shrek",
//            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
//            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
//            review = "I am incredibly fortunate to have such an inspiring mentor like Christiano. Their unwavering guidance and insightful advice have been the cornerstone of my professional growth. Their ability to navigate complex challenges with poise and their willingness to share knowledge has truly elevated my skills. Christiano leads with a perfect blend of patience and expertise, making them a true role model. Grateful for the opportunity to learn and be guided by the best!"
//        ),
        Testimonial(
            id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
            imageUrl = "https://raw.githubusercontent.com/ShaHar91/Portfolio/master/public/avatar3.png",
            fullName = "Els Schuurmans",
            company = Company(id = UUID.fromString("00000000-0000-0000-0000-000000000001")),
            jobPosition = JobPosition(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
            review = "I had the pleasure of working closely with Christiano Bolla in the process of making an personalised tracker application. He was very aware of the requirements I had, we discussed them thoroughly and he was able to give me some insights and good suggestions that broadend my view. During the process we could communicate in a very productive and convivial way. He kept me updated, had intermediate updates and was very clear in what feature was possible or would be a little more difficult to achieve. The end result was a very nice looking, usability friendly and functional application. I also managed to get a glimps of his coaching attitude when he was explaining certain technical steps in a friendly and understandable, yet concrete, way to me. Since a recent amount of time I have started working more in IT myself, as a test engineer. He has been a supportive help in the process as after -support. This even states his way of working in an agile method more. I would definetly advice him to others!"
        ),
    ).forEach { testimonial ->
        TestimonialsTable.insertIgnore {
            it[id] = testimonial.id
            it[imageUrl] = testimonial.imageUrl
            it[fullName] = testimonial.fullName
            it[companyId] = testimonial.company.id
            it[jobPositionId] = testimonial.jobPosition.id
            it[review] = testimonial.review
        }
    }

    // Services
    listOf(
        Service(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_mobile_banner.jpg",
            title = "Mobile development",
            shortDescription = "Whether it's a new app or optimizing an existing one, I offer Mobile development expertise. Let's collaborate to make your mobile application a success in the Google Play Store.",
            description = "My speciality is in the development of top class mobile applications that draw the audience. With stunning visuals and well timed animations I try to bring your idea to reality.\n\nCurrently my expertise lies in Native Android, but I am extending my repertoire with native iOS and Hybrid platforms.",
            bannerDescription = "Started my career with the Android Framework but I am not a stranger of trying new things. Dabbled quiet a bit with Ionic and Xamarin in the early days of my career but quickly focussed on Native Development.\n\nTo keep consitency between Android and iOS on some projects, I also checked out some Swift code and am able to understand quiet a bit about the project setup.\n\nAlso interested in checking out the \"newer kids on the block\" like Flutter and KMP to see what the benefits of both platforms/frameworks are and to make an educated decission when setting up a new project.",
            subServices = listOf(
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_android.svg",
                    title = "Android",
                    description = "Professionally working with the Android Framework since 2017, working on it as a hobbyist well before that. Touched a lot of the Framework during my career, ranging from the standard things like fetching and showing data to working with the Bluetooth and WiFi API. Also, I am no stranger to creating custom views using the Material Design System as a basis.\n\nIn order to increase my output while working on multiple projects for a client I also created a library which contained a lot of default classes and provided a basis for a project setup. This made it easier to switch between projects.\n\nI have experience in working with the default XML layout system but also jumped on the Compose train and noticed what all the hype about it was. Recently started trying out KMP to check whether it is something I want to learn more about.",
                    tag = Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009"))
                ),
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_ios.svg",
                    title = "iOS",
                    description = "While my prime focus is on Android, I had to look into an existing iOS code base to make sure the same changes where reflected in an Android project. Because of this I had to learn a bit about a Swift project, about the setup, and some specifics about the Swift Language.\n\nI am still interested to do a proper project in Native iOS, but didn't have the free time up till now.",
                    tag = Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000010"))
                ),
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_kmp.svg",
                    title = "KMP (Kotlin Multi Platform)",
                    description = "Even though this is the new kid on the block, it seems very promising. Started to follow a couple of tutorials and working on my first project using this framework. Stay tuned to see more.",
                    tag = Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000011"))
                ),
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000005"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_flutter.svg",
                    title = "Flutter",
                    description = "Not working with this for now, but is on my roadmap when I find the time for this!",
                    tag = Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000012"))
                ),
            ),
        ),
        Service(
            id = UUID.fromString("00000000-0000-0000-0000-000000000006"),
            imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_web_banner.jpg",
            title = "Web development",
            shortDescription = "From concept to deployment, I offer web development expertise to transform your online ideas into reality. Let's collaborate to create a user-friendly and feature-rich website that meets your needs.",
            description = "We provide innovative web applications for pioneers and founders, through means of the adequate blend of front-end and back-end technologies. We develop websites that are intuitive and simple to function.\n\nWith the latest software and technologies, our experienced team shares your idea and helps you get your business. Even if you start from scratch or upgrade your existing web applications, we ensure that all of our Web development projects use state-of-the-art technology and human-centered design.",
            bannerDescription = "Banner Description - Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            subServices = listOf(
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000007"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_kobweb.svg",
                    title = "Kobweb",
                    description = "Framework built to create websites with the use of the kotlin language together with the basis of Compose. Everything written gets compiled to javascript, html and css and the website is built up with a clean DOM structure.",
                    tag = Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000013"))
                )
            )
        ),
        Service(
            id = UUID.fromString("00000000-0000-0000-0000-000000000008"),
            imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_mobile_banner.jpg",
            title = "Backend development",
            shortDescription = "Need a to have system that maintains some persistent data, or maybe have a system that should act as an intermediary between 2 other systems? Let's see what is possible with my collaboration.",
            description = "Dabbled in different backend systems and making sure to understand all things regarding backend development.",
            bannerDescription = "Banner description",
            subServices = listOf(
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000010"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_flutter.svg",
                    title = "Laravel",
                    description = "Not working with this for now, but is on my roadmap when I find the time for this!"
                ),
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000011"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_flutter.svg",
                    title = "Node JS",
                    description = "Not working with this for now, but is on my roadmap when I find the time for this!"
                ),
                Service(
                    id = UUID.fromString("00000000-0000-0000-0000-000000000012"),
                    imageUrl = "https://raw.githubusercontent.com/ShaHar91/LandingPageCompose/develop/site/src/jsMain/resources/public/images/img_services_flutter.svg",
                    title = "Ktor",
                    description = "Not working with this for now, but is on my roadmap when I find the time for this!"
                ),
            ),
        ),
        Service(
            id = UUID.fromString("00000000-0000-0000-0000-000000000009"),
            imageUrl = "https://lh3.googleusercontent.com/d/14mFwr4cgIG54AmEVrVorrh8fxpY3mgSz",
            title = "Tutoring",
            shortDescription = "Looking to bring your (or your peer's) skills to the next level, or just need someone to look into a bug/error? Let's work together to maximize your potential and achieve your aspirations.",
            description = "Description - Looking to bring your (or your peer's) skills to the next level, or just need someone to look into a bug/error? Let's work together to maximize your potential and achieve your aspirations.",
            bannerDescription = "Banner Description - Looking to bring your (or your peer's) skills to the next level, or just need someone to look into a bug/error? Let's work together to maximize your potential and achieve your aspirations.",
            extraInfo = "Lorum ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."
        )
    ).forEach { service ->
        fun insertService(service: Service, parentId: UUID? = null) {
            ServicesTable.insertIgnore {
                it[id] = service.id
                it[imageUrl] = service.imageUrl
                it[title] = service.title
                it[shortDescription] = service.shortDescription
                it[description] = service.description
                it[bannerDescription] = service.bannerDescription
                it[extraInfo] = service.extraInfo
                it[tagId] = service.tag?.id
                it[parentServiceId] = parentId
            }
        }

        insertService(service)

        service.subServices?.forEach { subService ->
            insertService(subService, service.id)
        }
    }

    listOf(
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1BUbb3nM5DiWBVr2ZkQosh29zJu2Q6ZnZ",
            imageUrl = "https://lh3.googleusercontent.com/d/1kRceIhZXHwpZWW50Tc9f3xG2O1uUD12-",
            title = "Android Core",
            shortDescription = "Created this library in order to streamline and simplify the setup of new projects. Instead of copying a lot of classes and reimplementing it differently each time a single dependency was all we needed.",
            description = "Created this library in order to streamline and simplify the setup of new projects. Instead of copying a lot of classes and reimplementing it differently each time, a single dependency was all we needed.\n\nBecause the company worked on multiple projects a year, it was quite usual that we did a lot of manual setup like copy a lot of base and util classes over to the new project, maybe tweak them a little bit and then continue with the actual project. This resulted in a lot of differences in the code bases and scattered knowledge between coworkers. To circumvent this issue, I created a base library where the base classes and some util class (later extension functions) where placed. All accessible by a user friendly Builder class to initialize all needed pieces.\n\nAlso the network layer had a complete makeover to add a (streamlined) default way of work, taking pieces of all previous projects and coworkers to create a cohesive and robust basis. It had also some plug and play functionalities to cater to some project specific needs.\n\nThe library was comprised out of different modules. For example, there was the actual Core module, then we had the Networking module with retrofit usability. Besides that we also had modules for local data, starting with Realm which was Deprecated in favor of Room after a couple of versions.\n\nCheck the Github for more information and a sample!",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000004")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000004"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000002"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1XS0gfSlLee3E_YwdUgfRb9zHm1_TrHT8",
            imageUrl = "https://lh3.googleusercontent.com/d/1Yis96uGpu9T_sH6SU24sJzdSJJN8NeEJ",
            title = "Measurements",
            shortDescription = "Android had no standardized way to define Measurements and convert between other Measurements within the same Unit. I noticed iOS had a nice API for this, so I took that as an inspiration and essentialy created the iOS equivalent for Android.",
            description = "Android had no standardized way to define Measurements and convert between other Measurements within the same Unit. I noticed iOS had a nice API for this, so I took that as an inspiration and essentialy created the iOS equivalent for Android.\n\nConverting between Measurements in the same Unit is very easy by invoking a function which takes in the expected Unit as a parameter. Unit Tests have been created to ensure stability and quality. Functions for calculations have also been added. Adding, subtracting, ... are all possible with Measurements of the same Unit. In case a different scale is being used in a calculation (e.g.: nauticalMiles + miles), both scales will be converted to the base Unit (meters) and only then will they be added onto each other. Comparing (+ equals) scales within the same Unit is also possible.\n\nA Measurement can always be formatted in a standardized way with the correct symbol appended to it. There is also a possibility to add the amount of fraction digits in case you want the more fine grained results.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000001")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000005"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000003"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1TuciQOGztSYYGfGGNYk3ImMqd13wTILi",
            imageUrl = "https://lh3.googleusercontent.com/d/1_pKhvivjniijBarYV3AqANWOZ3GjSAsc",
            title = "Pokédex",
            shortDescription = "A try-out project with Jetpack Compose, where I use Koin for DI, and try to implement clean architecture by using a clear separation of concerns.",
            description = "This project was a nice opportunity to try out a couple of new things in combination with each other. For DI, Koin was used, as it is a lightweight Dependency Injection framework and does not add too much \"magical autogenerated code\" via the use of annotations. This way I could try to grasp what was happening under the hood more easily to actually try and understand dependency injection some more.\n\nJetpack Compose was used for the layout to see what it was and how it behaves in a project. In conjunction with Jetpack Compose, I also used the recommended way of a State object with the UiEVent and Event classes.\n\nThese new classes ensured the layout only gets updated whenever the state is being updated and actions will trigger an event which will then trigger a change in the state or trigger other events back to the UI. By doing things this way (the new classes + DI) it should be a lot easier to test all the other components of the applications. ViewModels, Repositories, Extension functions, Use Cases,... can all be isolated and tested seperate from each other.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000006")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000007")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000006")),
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000007"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000004"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/17hGybvkdXIgkotIOAAKJInPohUDhBQsX",
            imageUrl = "https://lh3.googleusercontent.com/d/1mydjliRyN8ETwzvFEOYXKCdN2iNIsVj6",
            title = "FoodWatcher",
            shortDescription = "With this project I implemented a different data persistent layer depending on the build flavor name. One is pure local Room database use, the other is a link to Firebase.",
            description = "With this project I implemented a different data persistent layer depending on the build flavor name. One is pure local Room database use, the other is a link to Firebase. This was mainly a try-out to understand the use of interfaces and actual implementation for different build flavors.\n\nIn this case, Koin is being used to actually add the correct dependencies per build flavor. Interfaces for the Repositories are being used where the function just expects specific return types which the app itself then can process into the needed data. For the Room Build flavor, DAO's are created and Entity objects, these objects can just be inserted, queried and deleted by using the built in annotations and the LiveData support. For Firebase, the collection of the Firestore is being queried, inserted,... and then converted into livedata which is then being returned by the Repository.\n\nAlso fastlane was added to simplify the process of getting builds out faster to the Firebase Release track.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000004")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000008"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000005"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1DSZfY4Gat5xubuGC3EtNiI-0t1d_XaAt",
            imageUrl = "https://lh3.googleusercontent.com/d/16dcRhKatn6_AyZN6XAtxPA6h0kyMYV0w",
            title = "PoemCollection Backend (Ktor)",
            shortDescription = "A backend created with the Ktor Server library to see what is possible with this technology. Still work in progress to actually request and process the data in an app.",
            description = "A backend created with the Ktor Server library to see what is possible with this technology. Still work in progress to actually request and process the data in an app.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000007")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000008")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000009"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000006"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1DSZfY4Gat5xubuGC3EtNiI-0t1d_XaAt",
            imageUrl = "https://lh3.googleusercontent.com/d/16dcRhKatn6_AyZN6XAtxPA6h0kyMYV0w",
            title = "PoemCollection Backend (NodeJS)",
            shortDescription = "A backend created with the NodeJS framework. My first backend with this technology proved very educational. Experimented with some things like virtual table/fields, authenticator (oauth) and more.",
            description = "A backend created with the NodeJS framework. My first backend with this technology proved very educational. Experimented with some things like virtual table/fields, authenticator (oauth) and more.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000016")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000017")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000018")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000019")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000010"))
            )
        ),
        Project(
            id = UUID.fromString("00000000-0000-0000-0000-000000000007"),
            bannerImageUrl = "https://lh3.googleusercontent.com/d/1YoRLRjRlRdpxAWv-F3Un8fhtOYJaIFgN",
            imageUrl = "https://lh3.googleusercontent.com/d/16dcRhKatn6_AyZN6XAtxPA6h0kyMYV0w",
            title = "PoemCollection Android",
            shortDescription = "An android application to catalog a bunch of Poems, also supports a review system and creating you own poems after logging in.",
            description = "An android application to catalog a bunch of Poems, also supports a review system and creating you own poems after logging in.",
            tags = listOf(
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000002")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000003")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000004")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000005")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000007")),
                Tag(id = UUID.fromString("00000000-0000-0000-0000-000000000009")),
            ),
            links = listOf(
                Link(id = UUID.fromString("00000000-0000-0000-0000-000000000011"))
            )
        ),
    ).forEach { project ->
        ProjectsTable.insertIgnore {
            it[id] = project.id
            it[imageUrl] = project.imageUrl
            it[bannerImageUrl] = project.bannerImageUrl
            it[title] = project.title
            it[shortDescription] = project.shortDescription
            it[description] = project.description
        }

        project.tags.forEach { tag ->
            TagsProjectsPivotTable.insertIgnore {
                it[tagId] = tag.id
                it[projectId] = project.id
            }
        }

        project.links.forEach { link ->
            LinksProjectsPivotTable.insertIgnore {
                it[linkId] = link.id
                it[projectId] = project.id
            }
        }
    }
}

suspend fun <T> dbQuery(block: () -> T): T = withContext(Dispatchers.IO) {
    transaction { block() }
}