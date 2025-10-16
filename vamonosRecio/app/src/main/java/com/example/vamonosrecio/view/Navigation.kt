package com.example.vamonosrecio.view

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vamonosrecio.db.AppDatabase

// Sealed class para definir las rutas de la aplicación
sealed class Screen(val route: String) {
    object Main : Screen("main_screen")
    object RouteList : Screen("route_list_screen")
    // object RouteDetail : Screen("route_detail_screen/{routeId}") // Ejemplo para futura pantalla de detalle
}

@Composable
fun AppNavigation(db: AppDatabase) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        // Pantalla principal con el mapa
        composable(Screen.Main.route) {
            MainScreen(
                db = db,
                onMenuClick = {
                    navController.navigate(Screen.RouteList.route)
                }
            )
        }

        // Pantalla con la lista de rutas
        composable(Screen.RouteList.route) {
            ListaRutaView(
                db = db,
                onBack = { navController.popBackStack() },
                onRouteClick = { ruta ->
                    // Aquí irá la lógica para navegar al detalle de la ruta
                    // Por ahora, lo dejamos vacío
                }
            )
        }
    }
}
