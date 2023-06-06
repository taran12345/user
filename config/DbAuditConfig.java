// All Rights Reserved, Copyright © Paysafe Holdings UK Limited 2017. For more information see LICENSE

package com.paysafe.upf.user.provisioning.config;

import com.paysafe.upf.user.provisioning.security.commons.CommonThreadLocal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class DbAuditConfig {

  /**
   * @return AuditorAware auditorAware.
   */
  @Bean
  public AuditorAware<String> auditorProvider() {
    return new AuditorAwareImpl();
  }

  private class AuditorAwareImpl implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
      if (CommonThreadLocal.getAuthLocal() == null || CommonThreadLocal.getAuthLocal().getUserName() == null) {
        return Optional.of("SYSTEM");
      } else {
        return Optional.ofNullable(CommonThreadLocal.getAuthLocal().getUserName());
      }
    }
  }
}