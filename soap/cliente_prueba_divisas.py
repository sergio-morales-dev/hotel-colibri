"""
cliente_prueba_divisas.py  ·  Hotel Colibrí — Pruebas del servicio SOAP de divisas
==========================================================================
Ejecuta con zeep las "Pruebas que deben pasar" de la actividad preliminar,
directo contra el servicio SOAP y ANTES de construir la API REST.
Si algo falla aquí, el problema está en Python y no en Spring Boot.

Requisito: divisas_service.py corriendo en otra terminal.
Ejecutar:  python cliente_prueba_divisas.py
"""

from decimal import Decimal

from zeep import Client
from zeep.exceptions import Fault

WSDL = "http://localhost:8001/?wsdl"


def probar(nombre, llamada, esperado):
    """Ejecuta una llamada y compara el resultado con lo esperado.
    'esperado' puede ser un valor o el código de un Fault (texto 'Client.X')."""
    try:
        obtenido = llamada()
    except Fault as f:
        # zeep entrega el código como "soap11env:Client.MontoInvalido";
        # nos quedamos con lo que va después de los dos puntos.
        obtenido = f"Fault {f.code.split(':')[-1]}"
    ok = str(obtenido) == str(esperado)
    print(f"[{'OK ' if ok else 'ERR'}] {nombre:<35} -> {obtenido}   (esperado: {esperado})")
    return ok


def main():
    cliente = Client(WSDL)
    s = cliente.service   # zeep genera un método por cada operación del WSDL

    casos = [
        ("listarMonedas()",              lambda: ", ".join(s.listarMonedas()), "MXN, USD, EUR, CAD"),
        ("convertir(5400, MXN, USD)",    lambda: s.convertir(Decimal("5400"), "MXN", "USD"), "300.00"),
        ("convertir(5400, MXN, EUR)",    lambda: s.convertir(Decimal("5400"), "MXN", "EUR"), "257.14"),
        ("convertir(5400, MXN, CAD)",    lambda: s.convertir(Decimal("5400"), "MXN", "CAD"), "415.38"),
        ("convertir(5400, MXN, JPY)",    lambda: s.convertir(Decimal("5400"), "MXN", "JPY"), "Fault Client.MonedaNoSoportada"),
        ("convertir(0, MXN, USD)",       lambda: s.convertir(Decimal("0"), "MXN", "USD"),    "Fault Client.MontoInvalido"),
        # Extra (proyecto final): tipo de cambio con 6 decimales
        ("obtenerTipoCambio(USD, MXN)",  lambda: s.obtenerTipoCambio("USD", "MXN"), "18.000000"),
        ("obtenerTipoCambio(MXN, EUR)",  lambda: s.obtenerTipoCambio("MXN", "EUR"), "0.047619"),
    ]

    resultados = [probar(*caso) for caso in casos]
    print(f"\n{sum(resultados)} de {len(resultados)} pruebas correctas")


if __name__ == "__main__":
    main()
