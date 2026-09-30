Parte 1:
#¿Cuántas razones distintas tiene esta clase para cambiar? (contar los conjuntos de responsabilidades: validación de stock, validación de cliente y mora, cálculo de precio, persistencia, notificación, registro).

La clase posee 340 líneas en total y concentra 6 diferentes responsabilidades dentro del método “procesarPedido()” violando el SRP, estas son:
•	Validación de stock (líneas 25-34): Consulta SQL directa a la tabla inventario.
•	Validación del cliente y mora (líneas 36-53): Consultas a clientes y facturas con regla horaria.
•	Cálculo de subtotal (líneas 55-63): Consultas iterativas a productos.
•	Calculo de descuento e impuesto (líneas 65-80): Reglas condicionales para tipos de clientes (VIP y frecuentes).
•	Persistencia directa (líneas 82-96): Operaciones SQL se añadir y actualizar manual sin repositorio ni gestión transaccional.
•	Notificación por correo (líneas 98-108): Construcción del mensaje usando StringBuilder y envió a la clase “EmailService”.

#¿Cuántos niveles de anidamiento condicional alcanza el bloque de cálculo de descuento? ¿Y el de validación de mora?

Existen dos partes de código que alcanzan un nivel 3 de anidamiento siendo: la validación mora (líneas 36-53) y el cálculo de descuento (líneas 65-80). En la primera parte del código se presenta debido a varios bloques condicionales (if/else) anidados entre sí, mientras en la otra se realiza una mezcla condicional de consultas SQL intermitentes dentro de la misma estructura lógica.

¿En cuántos niveles de abstracción distintos opera el método al mismo tiempo? (SQL embebido, reglas de negocio, formato de texto del correo, todo en la misma secuencia de líneas).

Se presenta un nivel 4 de abstracción simultanea donde se empieza por el acceso de datos de bajo nivel mediante sentencias SQL JDBC manuales, luego la lógica de negocio en la regla de corte de mora, tasa de IVA y rangos de montos de descuento, se sigue con el formateo de presentación ensamblando el manual del cuerpo del correo mediante StringBuilder, y finaliza con la orquestación de capturas de excepciones try-cathc para registro de auditorias y retorna el DTO (“ResultadoPedido”).

Si se necesitara agregar un nuevo tipo de cliente con reglas de descuento propias, ¿cuántas líneas existentes habría que modificar y en qué parte del método?

Se presenta un nivel 4 de abstracción simultanea donde se empieza por el acceso de datos de bajo nivel mediante sentencias SQL JDBC manuales, luego la lógica de negocio en la regla de corte de mora, tasa de IVA y rangos de montos de descuento, se sigue con el formateo de presentación ensamblando el manual del cuerpo del correo mediante StringBuilder, y finaliza con la orquestación de capturas de excepciones try-cathc para registro de auditorias y retorna el DTO (“ResultadoPedido”).

