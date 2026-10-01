# Post-contenido — Unidad 6: Antipatrones de Diseño

## Descripción
Repositorio del post-contenido de la Unidad 6 de Patrones de Diseño
de Software — Sexto Semestre. Un único proyecto Spring Boot
(pedidos-service/) con dos partes: diagnóstico y refactorización de
un antipatrón combinado en GestorPedidos, y diagnóstico y corrección
de un segundo antipatrón introducido al hacer crecer el mismo
proyecto con tres campañas de descuento.

## Decisiones de diseño

### Parte 1 — GestorPedidos
**Antipatrón identificado:** God Object y Spaghetti Code combinados.
GestorPedidos.procesarPedido() mezclaba 6 responsabilidades (validación
de stock, validación de cliente/mora, cálculo de subtotal, cálculo de
descuento con hasta 3 niveles de anidamiento, persistencia vía JDBC
embebido y notificación) en un único método de más de 100 líneas,
dentro de una clase de 340 líneas en total.

- La clase posee 340 líneas en total y concentra 6 diferentes responsabilidades dentro del método “procesarPedido()” violando el SRP, estas son:
•	Validación de stock (líneas 25-34): Consulta SQL directa a la tabla inventario.
•	Validación del cliente y mora (líneas 36-53): Consultas a clientes y facturas con regla horaria.
•	Cálculo de subtotal (líneas 55-63): Consultas iterativas a productos.
•	Calculo de descuento e impuesto (líneas 65-80): Reglas condicionales para tipos de clientes (VIP y frecuentes).
•	Persistencia directa (líneas 82-96): Operaciones SQL se añadir y actualizar manual sin repositorio ni gestión transaccional.
•	Notificación por correo (líneas 98-108): Construcción del mensaje usando StringBuilder y envió a la clase “EmailService”.

- Existen dos partes de código que alcanzan un nivel 3 de anidamiento siendo: la validación mora (líneas 36-53) y el cálculo de descuento (líneas 65-80). En la primera parte del código se presenta debido a varios bloques condicionales (if/else) anidados entre sí, mientras en la otra se realiza una mezcla condicional de consultas SQL intermitentes dentro de la misma estructura lógica.

- Se presenta un nivel 4 de abstracción simultanea donde se empieza por el acceso de datos de bajo nivel mediante sentencias SQL JDBC manuales, luego la lógica de negocio en la regla de corte de mora, tasa de IVA y rangos de montos de descuento, se sigue con el formateo de presentación ensamblando el manual del cuerpo del correo mediante StringBuilder, y finaliza con la orquestación de capturas de excepciones try-cathc para registro de auditorias y retorna el DTO (“ResultadoPedido”).

- Se presenta un nivel 4 de abstracción simultanea donde se empieza por el acceso de datos de bajo nivel mediante sentencias SQL JDBC manuales, luego la lógica de negocio en la regla de corte de mora, tasa de IVA y rangos de montos de descuento, se sigue con el formateo de presentación ensamblando el manual del cuerpo del correo mediante StringBuilder, y finaliza con la orquestación de capturas de excepciones try-cathc para registro de auditorias y retorna el DTO (“ResultadoPedido”).


**Patrón aplicado:** Chain of Responsibility para las validaciones
(dependencia real de orden y corte anticipado) y Strategy para el
cálculo de descuento por tipo de cliente (sin dependencia de orden).
Alternativa descartada: una lista de predicados booleanos para las
validaciones, sin corte anticipado real.

### Parte 2 — Crecimiento del proyecto
**Antipatrón identificado:** Golden Hammer. Las tres campañas de
descuento (Black Friday, Corporativo, Volumen) se implementaron como
eslabones adicionales de la cadena de validación existente, aunque
no tenían ninguna dependencia de orden entre sí ni necesidad de corte
anticipado — la propiedad que sí justificaba la cadena en
ValidadorStock y ValidadorCliente. Se reutilizó Chain of
Responsibility porque "ya funcionó" en la Parte 1, sin evaluar si
correspondía al nuevo problema.

- "PromocionBlackFriday", "PromocionCorporativo" y "PromocionVolumen", ni entre ellas y los validadores. Alterar la secuencia de ejecución (por ejemplo, ejecutar "PromocionVolumen" antes de "PromocionCorporativo") produce exactamente el mismo resultado, a diferencia de "ValidadorStock" y "ValidadorCliente", que sí requieren un orden estricto para permitir el corte anticipado cuando falta inventario.

- Se incluyeron en esta interfaz porque las clases simplemente aprovecharon que la cadena ya existia para engancharse y modificar un campo en el contexto compartido. Esto traicionó el contrato original de "ValidadorPedido" que consistía en evaluar y rechazar un pedido al introducir clases que jamás rechazan nada y solo escriben datos.

- La cadena no permite combinar reglas sin ambigüedad. Al estar diseñadas como eslabones que compiten por sobrescribir la misma variable mutable ("descuentoCampana" bajo la regla de "el mayor descuento gana"), intentar sumar o aplicar reglas compuestas obligaría a alterar el estado compartido del contexto y el acoplamiento entre eslabones.

- La solución se eligió únicamente porque "ya existía y funcionó la última vez". Se incurrió directamente en el antipatrón Golden Hammer al reutilizar "Chain of Responsibility" mecánicamente para resolver los nuevos descuentos, sin evaluar si el nuevo requerimiento encajaba mejor en el patrón "Strategy".

**Patrón aplicado:** Strategy, extendiendo SelectorEstrategiaDescuento
con CalculadorDescuentoFinal. Los tres eslabones mal aplicados y el
campo descuentoCampana se eliminaron del código (no se comentaron,
para no dejar un Lava Flow) y su historial queda documentado
únicamente en los commits de este repositorio.

## Cómo ejecutar
```
$ mvn spring-boot:run
$ mvn test
```

## Herramientas utilizadas
- Java 17, Spring Boot, Spring JDBC, Maven, H2 Database
- VS Code / IntelliJ IDEA, Git, GitHub

## Conclusiones
Se desarrollo y fortalecio la identificacion de los antipatrones
en este caso God Object, Sphagetti Code y Golden Hammer, los
cuales si dan soluciones al problema pero no es las mas recomendadas
debido a sus falencias en arquitectura y organizacion. Ademas añaden
complejidad y dificultades para mantener el codigo o añadir funciones.
