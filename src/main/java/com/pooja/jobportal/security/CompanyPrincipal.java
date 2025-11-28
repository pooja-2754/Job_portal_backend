package com.pooja.jobportal.security;

import com.pooja.jobportal.model.Company;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class CompanyPrincipal implements UserDetails {
    private final Company company;

    public CompanyPrincipal(Company company) {
        this.company = company;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_COMPANY"));
    }

    @Override
    public String getPassword() {
        return company.getPassword();
    }

    @Override
    public String getUsername() {
        return company.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Company getCompany() {
        return company;
    }

    public Long getId() {
        return company.getId();
    }

    public String getName() {
        return company.getName();
    }
}