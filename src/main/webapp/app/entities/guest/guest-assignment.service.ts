// src/main/webapp/app/entities/guest/guest-assignment.service.ts
import GuestService from './guest.service';
import SeatingTableService from '@/entities/seating-table/seating-table.service';
import { IGuest } from '@/shared/model/guest.model';
import { ISeatingTable } from '@/shared/model/seating-table.model';

export class GuestAssignmentService {
  private guestService: GuestService;
  private seatingTableService: SeatingTableService;

  constructor() {
    this.guestService = new GuestService();
    this.seatingTableService = new SeatingTableService();
  }

  public async assignGuestsToTables(): Promise<void> {
    const guestResponse = await this.guestService.retrieve();
    const guests: IGuest[] = guestResponse.data;

    const tableResponse = await this.seatingTableService.retrieve();
    const tables: ISeatingTable[] = tableResponse.data;

    const tableCapacities = new Map<number, number>();
    tables.forEach(t => tableCapacities.set(t.id, t.maxGuests || 10));

    guests.forEach(g => {
      g.table = null;
    });

    const costMatrix: number[][] = guests.map(g => tables.map(t => this.calculateCost(g, t, guests)));

    const assignments = this.hungarianAlgorithm(costMatrix);

    for (let i = 0; i < guests.length; i++) {
      const tableIndex = assignments[i];
      const guest = guests[i];
      const table = tables[tableIndex];

      if (!table || tableCapacities.get(table.id) <= 0) continue;

      guest.table = table;
      tableCapacities.set(table.id, tableCapacities.get(table.id) - 1);

      await this.guestService.update(guest);
    }

    const unassignedAccessibleGuests = guests.filter(g => g.accessibility && !g.table);
    if (unassignedAccessibleGuests.length > 0) {
      console.warn('יש אורחים נגישים שלא שובצו - לא היו מספיק שולחנות נגישים');
      alert('⚠️ לא כל האורחים שזקוקים לנגישות שובצו - אנא בדוק את השולחנות הנגישים.');
    }
  }

  private calculateCost(guest: IGuest, table: ISeatingTable, allGuests: IGuest[]): number {
    let cost = 0;

    if (table.currentGuestCount >= table.maxGuests) return Number.MAX_SAFE_INTEGER;
    if (guest.accessibility && !table.accessibility) return Number.MAX_SAFE_INTEGER;

    const sideRange = table.tableNumber <= 10 ? 'GROOM' : 'BRIDE';

    for (const other of allGuests) {
      if (other.id === guest.id) continue;

      const sameTable = other.table?.id === table.id;

      const avoids = guest.avoidGuests?.some(g => g.id === other.id) || other.avoidGuests?.some(g => g.id === guest.id);
      if (avoids) cost += 500;

      const prefers = guest.preferGuests?.some(g => g.id === other.id) || other.preferGuests?.some(g => g.id === guest.id);
      if (prefers) cost -= 50;

      if (sameTable && guest.relation && other.relation && guest.relation !== other.relation) {
        cost += 30;
      }
    }

    if (guest.side && guest.side !== sideRange) cost += 10;
    if (guest.nearStage && !table.nearStage) cost += 5;

    return cost;
  }
  private hungarianAlgorithm(costMatrix: number[][]): number[] {
    const n = costMatrix.length;
    const m = costMatrix[0].length;
    const assignments: number[] = new Array(n).fill(-1);
    const tableUsage = new Array(m).fill(0); // ספירת שימוש בכל שולחן

    const maxCapacities = costMatrix[0].map((_, j) => 2); // נניח 2 בכל שולחן, או שתעביר את זה מה־tableCapacities אם תרצה

    for (let i = 0; i < n; i++) {
      let minCost = Number.MAX_SAFE_INTEGER;
      let minIndex = -1;
      for (let j = 0; j < m; j++) {
        if (costMatrix[i][j] < minCost && tableUsage[j] < maxCapacities[j]) {
          minCost = costMatrix[i][j];
          minIndex = j;
        }
      }
      if (minIndex !== -1) {
        assignments[i] = minIndex;
        tableUsage[minIndex]++;
      }
    }

    return assignments;
  }
}
