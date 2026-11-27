
"""
pagos_service.py - Hotel Colibrí
Servicio SOAP de pagos y facturación.

Depende de Python 3.11, spyne y lxml.
Ejecutar: python pagos_service.py
"""

from spyne import Application, rpc, ServiceBase, Unicode, Decimal, Boolean, Fault
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

from wsgiref.simple_server import make_server
from decimal import Decimal as D
import re

# Puerto donde funcionará nuestro servicio.
PUERTO = 8002

# Monedas que acepta el hotel.
MONEDAS = ("MXN", "USD", "EUR", "CAD")

# Aquí guardaremos los pagos y las facturas temporalmente.
pagos = {}

# Contadores para generar folios consecutivos.
contador_pagos = 0
contador_facturas = 0


class PagosService(ServiceBase):

    @rpc(
        Unicode(min_occurs=1, nillable=False),
        Decimal(min_occurs=1, nillable=False),
        Unicode(min_occurs=1, nillable=False),
        Unicode(min_occurs=1, nillable=False),
        _returns=Unicode
    )
    def autorizarPago(ctx, referencia, monto, moneda, tokenTarjeta):
        global contador_pagos

        # El monto debe ser mayor que cero.
        if monto <= D("0"):
            raise Fault(
                faultcode="Client.MontoInvalido",
                faultstring="El monto debe ser mayor que cero."
            )

        # Solo aceptamos las monedas del hotel.
        if moneda not in MONEDAS:
            raise Fault(
                faultcode="Client.MonedaNoSoportada",
                faultstring="La moneda no está permitida."
            )

        # Comprobamos que el token sea uno de los de prueba.
        tokens_validos = (
            "tok_aprobada",
            "tok_fondos_insuficientes",
            "tok_rechazada"
        )

        if tokenTarjeta not in tokens_validos:
            raise Fault(
                faultcode="Client.TokenInvalido",
                faultstring="El token de la tarjeta no es válido."
            )

        # Una reserva no puede tener dos pagos aprobados.
        for pago in pagos.values():
            if (pago["referencia"] == referencia
                    and pago["estado"] == "APROBADO"):
                raise Fault(
                    faultcode="Client.PagoDuplicado",
                    faultstring="Esta reserva ya tiene un pago aprobado."
                )

        # Simulamos el rechazo de una tarjeta.
        if tokenTarjeta != "tok_aprobada":
            raise Fault(
                faultcode="Client.PagoRechazado",
                faultstring="El pago fue rechazado."
            )

        # Si todo está correcto, generamos el folio.
        contador_pagos += 1
        folio = f"PAG-{contador_pagos:06d}"

        pagos[folio] = {
            "referencia": referencia,
            "monto": monto,
            "moneda": moneda,
            "estado": "APROBADO",
            "folioFactura": None
        }

        return folio


    @rpc(
        Unicode(min_occurs=1, nillable=False),
        _returns=Unicode
    )
    def consultarPago(ctx, folioPago):

        # Revisamos si el folio existe.
        if folioPago not in pagos:
            raise Fault(
                faultcode="Client.PagoInexistente",
                faultstring="No existe un pago con ese folio."
            )

        # Devolvemos el estado actual del pago.
        return pagos[folioPago]["estado"]


# Configuramos el servicio SOAP del Hotel Colibrí.
application = Application(
    [PagosService],
    tns="http://hotelcolibri.mx/pagos",
    name="Pagos",
    in_protocol=Soap11(validator="lxml"),
    out_protocol=Soap11()
)

wsgi_application = WsgiApplication(application)

# Iniciamos el servidor en el puerto 8002.
if __name__ == "__main__":
    server = make_server("0.0.0.0", PUERTO, wsgi_application)
    print("Servicio SOAP de pagos funcionando")
    print("WSDL: http://localhost:8002/?wsdl")
    server.serve_forever()
