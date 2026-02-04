package com.singularbank.mifid.client.pdf.response;

import java.util.function.Function;

public sealed interface PdfResult<T>
    permits PdfResult.Success, PdfResult.Empty, PdfResult.Error {

  record Success<T>(T data) implements PdfResult<T> {

  }

  record Empty<T>(String reason) implements PdfResult<T> {

  }

  record Error<T>(String message, Exception cause) implements PdfResult<T> {

  }

  default <R> R match(
      Function<Success<T>, R> onSuccess,
      Function<Empty<T>, R> onEmpty,
      Function<Error<T>, R> onError) {

    return switch (this) {
      case Success<T> success -> onSuccess.apply(success);
      case Empty<T> empty -> onEmpty.apply(empty);
      case Error<T> error -> onError.apply(error);
    };
  }
}
