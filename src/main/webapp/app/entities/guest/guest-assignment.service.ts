import axios from 'axios';

const baseApiUrl = 'api/guests';

/**
 * Service for assigning guests to tables based on constraints.
 */
export class GuestAssignmentService {
  /**
   * Sends a request to assign all guests to tables using the backend algorithm.
   * Returns a list of warning messages for guests who couldn't be assigned.
   *
   * @returns Promise<string[]> - List of warning messages (may be empty).
   * @throws Error if the server returns an error or doesn't respond.
   */
  public async assignGuestsToTables(): Promise<string[]> {
    try {
      const response = await axios.post(`${baseApiUrl}/assign`);
      return response.data as string[];
    } catch (error: any) {
      if (error.response) {
        const status = error.response.status;
        const message = error.response.data?.message || error.response.statusText;
        console.error('Server error:', message);
        throw new Error(`Assignment failed: ${status} - ${message}`);
      } else if (error.request) {
        console.error('Request was sent but no response received:', error.request);
        throw new Error('No response received from server.');
      } else {
        console.error('Unexpected Axios error:', error.message);
        throw new Error('Unexpected error during guest assignment.');
      }
    }
  }
}
