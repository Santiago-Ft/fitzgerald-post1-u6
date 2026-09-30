package com.tienda.pedidos.service;

import com.tienda.pedidos.validacion.*;
import com.tienda.pedidos.descuento.*;
import com.tienda.pedidos.dto.*;

@org.springframework.stereotype.Service
public class GestorPedidos {

    private final ValidadorPedido primerValidador;
    private final SelectorEstrategiaDescuento selector;
    private final PedidoRepository repository;
    private final NotificacionPedidoService notificacion;

    public GestorPedidos(ValidadorStock stock, 
                          ValidadorCliente cliente,
                          SelectorEstrategiaDescuento selector, 
                          PedidoRepository repository,
                          NotificacionPedidoService notificacion) {
        this.primerValidador = stock.encadenar(cliente);
        this.selector = selector;
        this.repository = repository;
        this.notificacion = notificacion;
    }

    public ResultadoPedido procesarPedido(PedidoRequest request) {
        ContextoPedido contexto = new ContextoPedido(request);
        
        // 1. Validaciones encadenadas
        primerValidador.validar(contexto);
        if (contexto.isRechazado()) {
            return ResultadoPedido.rechazado(contexto.getMotivoRechazo());
        }

        // 2. Cálculo de subtotal a través del PedidoRepository existente
        double subtotal = calcularSubtotal(request);
        contexto.setSubtotal(subtotal);

        // 3. Estrategia de descuento según el tipo de cliente
        double descuento = selector.seleccionar(contexto.getTipoCliente()).calcular(contexto);
        double impuesto = (subtotal - (subtotal * descuento)) * 0.19;
        double total = (subtotal - (subtotal * descuento)) + impuesto;

        // 4. Persistencia y notificación
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