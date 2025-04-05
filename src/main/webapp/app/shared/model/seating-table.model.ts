import { type IEvent } from '@/shared/model/event.model';

export interface ISeatingTable {
  id?: number;
  tableNumber?: number;
  maxSeats?: number;
  nearStage?: boolean | null;
  accessibility?: boolean | null;
  event?: IEvent | null;
}

export class SeatingTable implements ISeatingTable {
  constructor(
    public id?: number,
    public tableNumber?: number,
    public maxSeats?: number,
    public nearStage?: boolean | null,
    public accessibility?: boolean | null,
    public event?: IEvent | null,
  ) {
    this.nearStage = this.nearStage ?? false;
    this.accessibility = this.accessibility ?? false;
  }
}
