<template>
  <div
    class="container-fluid"
    style="direction: ltr; min-height: 100vh; background: linear-gradient(to right, #f9f9f9, #e0f7fa); padding: 3rem"
  >
    <div class="row align-items-center">
      <!-- Text on the left -->
      <div class="col-md-7 text-start">
        <h1 class="display-3 mb-4" style="color: #00796b; font-weight: bold">Welcome to the Guest Seating Arrangement System</h1>
        <p class="lead mb-4" style="font-size: 1.3rem">
          Here you can add guests, define constraints such as proximity to stage, friend groups, and tables – and smartly assign them.
        </p>

        <div class="mb-4">
          <div class="alert alert-success" v-if="authenticated">
            <span v-if="username">Hello {{ username }}! You are successfully logged in.</span>
          </div>

          <div v-if="!authenticated">
            <div class="alert alert-warning text-start">
              <span>You must log in to use the system.</span>
              <button class="btn btn-primary btn-sm ms-2" @click="openLogin()">Log In</button>
            </div>
            <div class="alert alert-info text-start mt-2">
              <span>Don't have an account yet?</span>
              <router-link class="btn btn-outline-secondary btn-sm ms-2" to="/register">Sign Up</router-link>
            </div>
          </div>
        </div>

        <p class="text-muted">For questions or support – we're here to help.</p>
      </div>

      <!-- Image on the right -->
      <div class="col-md-5 text-center mt-4 mt-md-0">
        <img src="../../../content/images/image.png" alt="Event" class="img-fluid shadow rounded" style="max-width: 100%; height: auto" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>
