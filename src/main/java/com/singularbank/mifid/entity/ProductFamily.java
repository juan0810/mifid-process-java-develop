package com.singularbank.mifid.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Getter
@RequiredArgsConstructor
public enum ProductFamily {
  A("Depósitos bancarios e imposiciones a plazo fijo"),

  B("""
      Renta fija pública o privada no compleja o deuda emitida por entidades de crédito no subordinada \
      y sin derivados implícitos y no susceptible de absorción de pérdidas y/o conversión en acciones del emisor"""),

  C("Renta variable cotizada"),

  D("""
      Fondos de inversión UCITS, planes de pensiones, productos de inversión basados en seguros o ETF \
      (fondos cotizados)"""),

  E("""
      Renta Fija privada compleja (incluyendo con opción de amortización anticipada por parte del emisor \
      o el inversor)"""),

  F("""
      Productos Estructurados con o sin garantía de principal (ya sean planes de pensiones, fondos, ETF, \
      ETNs, seguros, depósitos o bonos), fondos, ETF (fondos cotizados) no UCITS o el resto de ETPs \
      (productos negociados en bolsa que replican el comportamiento de una cesta de activos, como ETC en \
      caso de materias primas como subyacente o ETN que son emisiones de deuda)"""),

  G("""
      Bonos convertibles, bonos de titulización, participaciones preferentes, deuda subordinada u otros \
      bonos de entidades de crédito (incluidos los pasivos susceptibles de absorción de pérdidas y/o \
      conversión en acciones del emisor)"""),

  H("Fondos de inversión inmobiliarios"),

  I("""
      Productos de capital riesgo, de gestión alternativa como fondos de inversión libre (que inviertan \
      en acciones, préstamos u otros subyacentes) o renta variable no cotizada"""),

  J("Derivados cotizados en mercados organizados"),

  K("CFDs u otros Derivados no cotizados (OTC)");

  private final String description;

  public static ProductFamily fromCode(String code) {
    if (StringUtils.isBlank(code)) {
      return null;
    }
    try {
      return ProductFamily.valueOf(code.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}