package catalystpage.com.model

enum class Section(
    val id: String,
    val title: String,
    val subtitle: String,
    val desc: String,
    val path: String
) {
    Home(
        id = "home",
        title = "",
        subtitle = "",
        desc = "",
        path = "/#home"
    ),
    About(
        id = "about",
        title = "About Catalyst",
        subtitle = "Why Choose Us?",
        desc = "",
        path = "/#about"
    ),
    Products(
        id = "products",
        title = "Products",
        subtitle = "",
        desc = "",
        path = "/#products"
    ),
    Articles(
        id = "articles",
        title = "Articles",
        subtitle = "Relevant Kombucha Articles",
        desc = "Summarized and Reviewed by Mary Rose Aquino, RPm",
        path = "/articles"
    ),
    Partners(
        id = "partners",
        title = "Partners",
        subtitle = "",
        desc = "",
        path = "#partners"
    ),
    Contact(
        id = "contact",
        title = "Contact Us",
        subtitle = "",
        desc = "",
        path = "/#contact"
    ),
    SignIn(
        id = "signIn",
        title = "Sign In",
        subtitle = "",
        desc = "",
        path = "/signIn"
    ),
    Dashboard(
    id = "dashboard",
    title = "",
    subtitle = "",
    desc = "",
    path = "/dashboard"
    )
}