package catalystpage.com.model

data class Zone(
    val name: String,
    val cities: List<String>
)

val ncrZones = listOf(
    Zone(
        name = "Zone 1: North Caloocan & Valenzuela",
        cities = listOf("North Caloocan", "Valenzuela")
    ),
    Zone(
        name = "Zone 2: Quezon City North",
        cities = listOf("Novaliches", "Batasan Hills")
    ),
    Zone(
        name = "Zone 3: Quezon City Central & San Juan",
        cities = listOf("Quezon City Central", "San Juan", "Cubao")
    ),
    Zone(
        name = "Zone 4: Quezon City South & East Caloocan",
        cities = listOf("Quezon City South", "East Caloocan", "Diliman", "Kamuning")
    ),
    Zone(
        name = "Zone 5: Manila Central",
        cities = listOf("Morayta", "Ermita", "Paco")
    ),
    Zone(
        name = "Zone 6: Pasay, Parañaque & Las Piñas",
        cities = listOf("Pasay", "Parañaque", "Las Piñas")
    ),
    Zone(
        name = "Zone 7: Makati & Taguig",
        cities = listOf("Makati", "Taguig", "Fort Bonifacio", "BGC", "Pateros")
    ),
    Zone(
        name = "Zone 8: Eastern NCR",
        cities = listOf("Marikina", "Pasig", "Cainta", "Muntinlupa")
    )
)