import { type IEvent } from '@/shared/model/event.model';
import { type ISeatingTable } from '@/shared/model/seating-table.model';

import { type GuestStatus } from '@/shared/model/enumerations/guest-status.model';
import { type GuestSide } from '@/shared/model/enumerations/guest-side.model';
import { type GuestRelation } from '@/shared/model/enumerations/guest-relation.model';
export interface IGuest {
  id?: number;
  lastNameAndFirstName?: string;
  numberOfSeats?: number;
  phone?: string | null;
  nearStage?: boolean | null;
  status?: keyof typeof GuestStatus;
  side?: keyof typeof GuestSide | null;
  relation?: keyof typeof GuestRelation;
  accessibility?: boolean;
  event?: IEvent | null;
  table?: ISeatingTable | null;
  avoidGuests?: IGuest[] | null;
  preferGuests?: IGuest[] | null;
  avoidedBies?: IGuest[] | null;
  preferredBies?: IGuest[] | null;
}

export class Guest implements IGuest {
  constructor(
    public id?: number,
    public lastNameAndFirstName?: string,
    public numberOfSeats?: number,
    public phone?: string | null,
    public nearStage?: boolean | null,
    public status?: keyof typeof GuestStatus,
    public side?: keyof typeof GuestSide | null,
    public relation?: keyof typeof GuestRelation,
    public accessibility?: boolean,
    public event?: IEvent | null,
    public table?: ISeatingTable | null,
    public avoidGuests?: IGuest[] | null,
    public preferGuests?: IGuest[] | null,
    public avoidedBies?: IGuest[] | null,
    public preferredBies?: IGuest[] | null,
  ) {
    this.nearStage = this.nearStage ?? false;
    this.accessibility = this.accessibility ?? false;
  }
}
