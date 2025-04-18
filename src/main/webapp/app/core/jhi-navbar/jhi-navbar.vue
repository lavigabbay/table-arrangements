<template>
  <b-navbar data-cy="navbar" toggleable="md" type="dark" class="jh-navbar">
    <b-navbar-brand class="logo d-flex align-items-center" b-link to="/">
      <img src="/content/images/logo-jhipster.png" alt="לוגו" style="height: 70px; width: auto; margin-inline-end: 12px" />
      <span class="navbar-title" v-text="t$('global.title')"></span>
    </b-navbar-brand>

    <b-navbar-toggle
      right
      class="jh-navbar-toggler d-lg-none"
      href="javascript:void(0);"
      data-toggle="collapse"
      target="header-tabs"
      aria-expanded="false"
      aria-label="Toggle navigation"
    >
      <font-awesome-icon icon="bars" />
    </b-navbar-toggle>

    <b-collapse is-nav id="header-tabs">
      <b-navbar-nav class="ml-auto">
        <!-- תפריט ראשי -->
        <b-nav-item to="/" exact>
          <span>
            <font-awesome-icon icon="home" />
            <span v-text="t$('global.menu.home')"></span>
          </span>
        </b-nav-item>

        <!-- ישויות -->
        <b-nav-item-dropdown right id="entity-menu" v-if="authenticated" active-class="active" class="pointer" data-cy="entity">
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="th-list" />
              <span class="no-bold" v-text="t$('global.menu.entities.main')"></span>
            </span>
          </template>
          <entities-menu />
        </b-nav-item-dropdown>

        <!-- אדמין -->
        <b-nav-item-dropdown
          right
          id="admin-menu"
          v-if="hasAnyAuthority('ROLE_ADMIN') && authenticated"
          :class="{ 'router-link-active': subIsActive('/admin') }"
          active-class="active"
          class="pointer"
          data-cy="adminMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="users-cog" />
              <span class="no-bold" v-text="t$('global.menu.admin.main')"></span>
            </span>
          </template>
          <b-dropdown-item to="/admin/user-management" active-class="active">
            <font-awesome-icon icon="users" />
            <span v-text="t$('global.menu.admin.userManagement')"></span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/metrics" active-class="active">
            <font-awesome-icon icon="tachometer-alt" />
            <span v-text="t$('global.menu.admin.metrics')"></span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/health" active-class="active">
            <font-awesome-icon icon="heart" />
            <span v-text="t$('global.menu.admin.health')"></span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/configuration" active-class="active">
            <font-awesome-icon icon="cogs" />
            <span v-text="t$('global.menu.admin.configuration')"></span>
          </b-dropdown-item>
          <b-dropdown-item to="/admin/logs" active-class="active">
            <font-awesome-icon icon="tasks" />
            <span v-text="t$('global.menu.admin.logs')"></span>
          </b-dropdown-item>
          <b-dropdown-item v-if="openAPIEnabled" to="/admin/docs" active-class="active">
            <font-awesome-icon icon="book" />
            <span v-text="t$('global.menu.admin.apidocs')"></span>
          </b-dropdown-item>
        </b-nav-item-dropdown>

        <!-- שפות -->
        <b-nav-item-dropdown id="languagesnavBarDropdown" right v-if="languages && Object.keys(languages).length > 1">
          <template #button-content>
            <font-awesome-icon icon="flag" />
            <span class="no-bold" v-text="t$('global.menu.language')"></span>
          </template>
          <b-dropdown-item
            v-for="(value, key) in languages"
            :key="`lang-${key}`"
            @click="changeLanguage(key)"
            :class="{ active: isActiveLanguage(key) }"
          >
            {{ value.name }}
          </b-dropdown-item>
        </b-nav-item-dropdown>

        <!-- חשבון משתמש -->
        <b-nav-item-dropdown
          right
          href="javascript:void(0);"
          id="account-menu"
          :class="{ 'router-link-active': subIsActive('/account') }"
          active-class="active"
          class="pointer"
          data-cy="accountMenu"
        >
          <template #button-content>
            <span class="navbar-dropdown-menu">
              <font-awesome-icon icon="user" />
              <span class="no-bold" v-text="t$('global.menu.account.main')"></span>
            </span>
          </template>
          <b-dropdown-item to="/account/settings" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="wrench" />
            <span v-text="t$('global.menu.account.settings')"></span>
          </b-dropdown-item>
          <b-dropdown-item to="/account/password" v-if="authenticated" active-class="active">
            <font-awesome-icon icon="lock" />
            <span v-text="t$('global.menu.account.password')"></span>
          </b-dropdown-item>
          <b-dropdown-item v-if="authenticated" @click="logout()" active-class="active">
            <font-awesome-icon icon="sign-out-alt" />
            <span v-text="t$('global.menu.account.logout')"></span>
          </b-dropdown-item>
          <b-dropdown-item v-if="!authenticated" @click="openLogin()" active-class="active">
            <font-awesome-icon icon="sign-in-alt" />
            <span v-text="t$('global.menu.account.login')"></span>
          </b-dropdown-item>
          <b-dropdown-item v-if="!authenticated" to="/register" active-class="active">
            <font-awesome-icon icon="user-plus" />
            <span v-text="t$('global.menu.account.register')"></span>
          </b-dropdown-item>
        </b-nav-item-dropdown>
      </b-navbar-nav>
    </b-collapse>
  </b-navbar>
</template>

<script lang="ts" src="./jhi-navbar.component.ts"></script>

<style scoped>
.navbar-version {
  font-size: 0.65em;
  color: #ccc;
}

.jh-navbar {
  background-color: #353d47;
  padding: 0.2em 1em;
}

.jh-navbar .navbar-nav .nav-item {
  margin-left: 1.5rem;
}

.jh-navbar .dropdown-item.active,
.jh-navbar .dropdown-item.active:focus,
.jh-navbar .dropdown-item.active:hover {
  background-color: #353d47;
}

.jh-navbar .dropdown-toggle::after {
  margin-left: 0.15em;
}

.navbar-title {
  color: white;
  font-weight: bold;
  font-size: 1.25rem;
}

.navbar-brand.logo {
  padding: 0 7px;
}
</style>
