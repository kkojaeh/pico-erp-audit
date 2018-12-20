package pico.erp.audit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public interface AuditExceptions {

  @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, reason = "already.audit.alias.exists.exception")
  class AlreadyAuditAliasExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

  }

  @ResponseStatus(code = HttpStatus.UNPROCESSABLE_ENTITY, reason = "not.registed.audit.type.exception")
  class NotRegisteredAuditTypeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

  }
}
