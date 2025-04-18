<template>
  <div
    class="container-fluid"
    style="direction: rtl; min-height: 100vh; background: linear-gradient(to left, #f9f9f9, #e0f7fa); padding: 3rem"
  >
    <div class="row align-items-center">
      <!-- טקסט בצד ימין -->
      <div class="col-md-7 text-end">
        <h1 class="display-3 mb-4" style="color: #00796b; font-weight: bold">ברוכים הבאים למערכת שיבוץ אורחים לאירועים</h1>
        <p class="lead mb-4" style="font-size: 1.3rem">
          כאן תוכלו להזין אורחים, להגדיר אילוצים כמו קרבה לבמה, קבוצות חברים ושולחנות – ולשבץ את האורחים בקלות ובחכמה.
        </p>

        <div class="mb-4">
          <div class="alert alert-success" v-if="authenticated">
            <span v-if="username">שלום {{ username }}! אתה מחובר בהצלחה.</span>
          </div>

          <div v-if="!authenticated">
            <div class="alert alert-warning text-end">
              <span>כדי להשתמש במערכת, יש להתחבר.</span>
              <button class="btn btn-primary btn-sm ms-2" @click="openLogin()">התחבר</button>
            </div>
            <div class="alert alert-info text-end mt-2">
              <span>אין לך עדיין משתמש?</span>
              <router-link class="btn btn-outline-secondary btn-sm ms-2" to="/register">הרשמה</router-link>
            </div>
          </div>
        </div>

        <p class="text-muted">לשאלות או תמיכה – אנחנו כאן בשבילכם.</p>
      </div>

      <!-- תמונה בצד שמאל -->
      <div class="col-md-5 text-center mt-4 mt-md-0">
        <img src="../../../content/images/image.png" alt="אירוע" class="img-fluid shadow rounded" style="max-width: 100%; height: auto" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" src="./home.component.ts"></script>
