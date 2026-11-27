"""
divisas_service.py  ·  Hotel Colibrí — Servicio SOAP de divisas
================================================================
Sistema "heredado" del hotel: convierte importes entre pesos mexicanos y
otras monedas. Lo consume la API REST (Spring Boot) a partir de su WSDL.

Contrato (sección 5.1 del proyecto final; no cambiar nombres ni tipos):
    Archivo y puerto : divisas_service.py · 8001 · WSDL en http://localhost:8001/?wsdl
    Espacio de nombres: tns="http://hotelcolibri.mx/divisas" · name="Divisas" · clase DivisasService
    listarMonedas()                     -> Array(Unicode)
    obtenerTipoCambio(origen, destino)  -> xs:decimal, 6 decimales
    convertir(monto, origen, destino)   -> xs:decimal, 2 decimales
    Faults: Client.MonedaNoSoportada · Client.MontoInvalido

Depende de: Python 3.11 (spyne NO funciona con 3.12 o superior), spyne y lxml.
Ejecutar:   python divisas_service.py   (con el venv activado)
"""

# Piezas de spyne. Unicode, Decimal y Array son los TIPOS que se escriben en el WSDL.
from spyne import Application, rpc, ServiceBase, Unicode, Decimal, Array, Fault
from spyne.protocol.soap import Soap11
from spyne.server.wsgi import WsgiApplication

from wsgiref.simple_server import make_server

# Decimal de Python con alias D: guarda VALORES exactos de dinero.
# El alias evita que choque con el Decimal de spyne, que describe TIPOS.
from decimal import Decimal as D, ROUND_HALF_UP

PUERTO = 8001
DOS_DECIMALES = D("0.01")       # plantilla de redondeo para importes
SEIS_DECIMALES = D("0.000001")  # plantilla de redondeo para tipos de cambio


def validar_moneda(codigo, monedas):
    """Lanza el Fault del contrato si el código no está en la tabla.
    Es una función aparte porque la usan convertir y obtenerTipoCambio."""
    if codigo not in monedas:
        # Se usa Fault y NO ValueError: spyne esconde un ValueError como
        # "Internal Error"; con Fault el código viaja dentro de la respuesta SOAP.
        raise Fault(faultcode="Client.MonedaNoSoportada",
                    faultstring=f"Moneda no soportada: {codigo}. "
                                f"Use una de: {', '.join(monedas)}")


class DivisasService(ServiceBase):

    # Pesos mexicanos que vale UNA unidad de cada moneda (tasas ficticias del documento).
    # Se escriben como texto para que Decimal guarde el valor exacto.
    monedas = {
        "MXN": D("1.00"),
        "USD": D("18.00"),
        "EUR": D("21.00"),
        "CAD": D("13.00"),
    }

    @rpc(_returns=Array(Unicode))
    def listarMonedas(ctx):
        # .keys() da los códigos del diccionario; list() los vuelve arreglo.
        return list(DivisasService.monedas.keys())

    # Los parámetros son OBLIGATORIOS en el WSDL (min_occurs=1, nillable=False):
    # si falta uno, lxml rechaza la petición antes de llegar al método.
    @rpc(Unicode(min_occurs=1, nillable=False),
         Unicode(min_occurs=1, nillable=False),
         _returns=Decimal)
    def obtenerTipoCambio(ctx, origen, destino):
        # Unidades de destino que vale 1 unidad de origen. Ej.: USD -> MXN = 18.000000
        validar_moneda(origen, DivisasService.monedas)
        validar_moneda(destino, DivisasService.monedas)

        tasas = DivisasService.monedas
        tipo = tasas[origen] / tasas[destino]
        return tipo.quantize(SEIS_DECIMALES, rounding=ROUND_HALF_UP)

    @rpc(Decimal(min_occurs=1, nillable=False),
         Unicode(min_occurs=1, nillable=False),
         Unicode(min_occurs=1, nillable=False),
         _returns=Decimal)
    def convertir(ctx, monto, origen, destino):
        # Error del documento: el monto debe ser mayor que cero
        if monto <= 0:
            raise Fault(faultcode="Client.MontoInvalido",
                        faultstring=f"El monto debe ser mayor que cero (recibido: {monto})")

        validar_moneda(origen, DivisasService.monedas)
        validar_moneda(destino, DivisasService.monedas)

        tasas = DivisasService.monedas
        # Fórmula del documento: monto × tasa(origen) ÷ tasa(destino),
        # redondeado a 2 decimales "medio hacia arriba".
        resultado = monto * tasas[origen] / tasas[destino]
        return resultado.quantize(DOS_DECIMALES, rounding=ROUND_HALF_UP)


# Aplicación Spyne con el espacio de nombres y el nombre que exige el contrato.
application = Application([DivisasService],
                          tns="http://hotelcolibri.mx/divisas",
                          name="Divisas",
                          in_protocol=Soap11(validator="lxml"),  # valida cada petición
                          out_protocol=Soap11())

# Se envuelve en una aplicación WSGI para que la sirva un servidor HTTP.
wsgi_application = WsgiApplication(application)

if __name__ == "__main__":
    server = make_server("0.0.0.0", PUERTO, wsgi_application)
    print(f"Servicio de divisas escuchando en http://localhost:{PUERTO}")
    print(f"WSDL disponible en http://localhost:{PUERTO}/?wsdl")
    print("Detener con Ctrl + C")
    server.serve_forever()
