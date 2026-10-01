package com.tienda.pedidos.validacion;

@org.springframework.stereotype.Component
public class PromocionBlackFriday extends ValidadorPedido {
    private final boolean campanaActiva; // inyectado desde application.properties

    public PromocionBlackFriday(@org.springframework.beans.factory.annotation.Value("${promo.black-friday.activa}") boolean campanaActiva) {
        this.campanaActiva = campanaActiva;
    }

    @Override
    protected void ejecutar(ContextoPedido contexto) {
        if (campanaActiva) {
            contexto.aplicarDescuentoCampana(0.25);
        }
        // nunca rechaza -- este eslabon no valida nada, solo aprovecha que la cadena
        // ya existe para "engancharse" y modificar el contexto
    }
}
