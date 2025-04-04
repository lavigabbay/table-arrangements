import { type IVenueTable } from '@/shared/model/venue-table.model';

export interface IEventTable {
  id?: number;
  tableNumber?: number;
  maxSeats?: number;
  venue?: IVenueTable | null;
}

export class EventTable implements IEventTable {
  constructor(
    public id?: number,
    public tableNumber?: number,
    public maxSeats?: number,
    public venue?: IVenueTable | null,
  ) {}
}
