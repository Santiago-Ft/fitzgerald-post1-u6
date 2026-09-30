package com.tienda.pedidos.dto;

import java.util.List;

public class PedidoRequest {
    private Long clienteId;
    private String clienteEmail;
    private List<ItemPedido> items;

    public PedidoRequest() {}

    public PedidoRequest(Long clienteId, String clienteEmail, List<ItemPedido> items) {
        this.clienteId = clienteId;
        this.clienteEmail = clienteEmail;
        this.items = items;
    }

    public Long getClienteId() { return clienteId; }
    public String getClienteEmail() { return clienteEmail; }
    public List<ItemPedido> getItems() { return items; }
}