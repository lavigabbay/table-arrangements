import { type IVenueTable } from '@/shared/model/venue-table.model';
import { type IEventTable } from '@/shared/model/event-table.model';

import { type GuestStatus } from '@/shared/model/enumerations/guest-status.model';
import { type GuestRelation } from '@/shared/model/enumerations/guest-relation.model';
export interface IGuestTable {
  id?: number;
  lastNameAndFirstName?: string;
  numberOfSeats?: number;
  phone?: string;
  nearDanceFloor?: boolean | null;
  status?: keyof typeof GuestStatus;
  side?: string;
  relation?: keyof typeof GuestRelation;
  notWithId?: number | null;
  withId?: number | null;
  conditions?: string | null;
  accessibility?: boolean;
  venueName?: IVenueTable | null;
  eventTable?: IEventTable | null;
}

export class GuestTable implements IGuestTable {
  constructor(
    public id?: number,
    public lastNameAndFirstName?: string,
    public numberOfSeats?: number,
    public phone?: string,
    public nearDanceFloor?: boolean | null,
    public status?: keyof typeof GuestStatus,
    public side?: string,
    public relation?: keyof typeof GuestRelation,
    public notWithId?: number | null,
    public withId?: number | null,
    public conditions?: string | null,
    public accessibility?: boolean,
    public venueName?: IVenueTable | null,
    public eventTable?: IEventTable | null,
  ) {
    this.nearDanceFloor = this.nearDanceFloor ?? false;
    this.accessibility = this.accessibility ?? false;
  }
}
