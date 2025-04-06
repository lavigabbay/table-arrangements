export class GuestAssignmentService {
  public async assignGuestsToTables(): Promise<void> {
    try {
      const response = await fetch('/api/guests/assign', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // ← השורה הזאת מאפשרת שליחת קובצי cookie (הרשאה)
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('שגיאה מהשרת:', errorText);
        throw new Error(`שגיאה בשיבוץ: ${response.status} - ${response.statusText}`);
      }
    } catch (error: any) {
      console.error('שגיאה כללית במהלך שליחת הבקשה לשיבוץ אורחים:', error);
      throw error;
    }
  }
}
