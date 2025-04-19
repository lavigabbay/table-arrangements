import axios from 'axios';

const baseApiUrl = 'api/guests';

export class GuestAssignmentService {
  public async assignGuestsToTables(): Promise<void> {
    try {
      await axios.post(`${baseApiUrl}/assign`);
    } catch (error: any) {
      if (error.response) {
        const status = error.response.status;
        const message = error.response.data?.message || error.response.statusText;
        console.error('שגיאה מהשרת:', message);
        throw new Error(`שגיאה בשיבוץ: ${status} - ${message}`);
      } else if (error.request) {
        console.error('הבקשה נשלחה אך לא התקבלה תגובה מהשרת:', error.request);
        throw new Error('לא התקבלה תגובה מהשרת.');
      } else {
        console.error('שגיאה כללית ב־axios:', error.message);
        throw new Error('שגיאה כללית בקריאה לשיבוץ אורחים.');
      }
    }
  }
}
