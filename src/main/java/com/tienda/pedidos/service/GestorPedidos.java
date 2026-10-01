package com.tienda.pedidos.service;

import com.tienda.pedidos.descuento.*;
import com.tienda.pedidos.dto.*;
import com.tienda.pedidos.validacion.*;
import org.springframework.stereotype.Service;

@Service
public class GestorPedidos {

    private final ValidadorPedido primerValidador;
    private final SelectorEstrategiaDescuento selector;
    private final PedidoRepository repository;
    private final NotificacionPedidoService notificacion;

    public GestorPedidos(ValidadorStock stock, 
                          ValidadorCliente cliente,
                          PromocionBlackFriday blackFriday, 
                          PromocionCorporativo corporativo,
                          PromocionVolumen volumen, 
                          SelectorEstrategiaDescuento selector,
                          PedidoRepository repository, 
                          NotificacionPedidoService notificacion) {
        
        // Encadenamiento ampliado con las nuevas promociones de campaña
        this.primerValidador = stock.encadenar(cliente)
                                    .encadenar(blackFriday)
                                    .encadenar(corporativo)
                                    .encadenar(volumen);
        this.selector = selector;
        this.repository = repository;
        this.notificacion = notificacion;
    }

    public ResultadoPedido procesarPedido(PedidoRequest request) {
    ContextoPedido contexto = new ContextoPedido(request);

    // 1. Ejecución de la cadena de validadores y promociones
    primerValidador.validar(contexto);
    if (contexto.isRechazado()) {
        return ResultadoPedido.rechazado(contexto.getMotivoRechazo());
    }

    // 2. Cálculo de subtotal en memoria a partir de los ítems
    double subtotal = calcularSubtotal(request);
    contexto.setSubtotal(subtotal);

    // 3. Selección del mejor descuento con fallback seguro para evitamiento de NullPointerException
    String tipoCliente = contexto.getTipoCliente() != null ? contexto.getTipoCliente() : "ESTANDAR";
    double descuentoTipoCliente = selector.seleccionar(tipoCliente).calcular(contexto);
    double descuento = Math.max(descuentoTipoCliente, contexto.getDescuentoCampana());

    // 4. Cálculo de impuesto y total final
    double subtotalConDescuento = subtotal - (subtotal * descuento);
    double impuesto = subtotalConDescuento * 0.19;
    double total = subtotalConDescuento + impuesto;

    // 5. Persistencia en base de datos y notificación
    Long pedidoId = repository.guardar(contexto, descuento, impuesto, total);
    notificacion.notificarConfirmacion(contexto, pedidoId, descuento, impuesto, total);

    return ResultadoPedido.confirmado(pedidoId, total);
}

    private double calcularSubtotal(PedidoRequest request) {
        return request.getItems().stream()
                .mapToDouble(item -> item.getPrecioUnitario() * item.getCantidad())
                .sum();
    }
}