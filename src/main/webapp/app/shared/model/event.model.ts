import { type IUser } from '@/shared/model/user.model';

export interface IEvent {
  id?: number;
  eventName?: string;
  eventOwners?: string | null;
  groomParents?: string | null;
  brideParents?: string | null;
  weddingDate?: Date;
  receptionTime?: Date;
  weddingTime?: Date;
  user?: IUser | null;
}

export class Event implements IEvent {
  constructor(
    public id?: number,
    public eventName?: string,
    public eventOwners?: string | null,
    public groomParents?: string | null,
    public brideParents?: string | null,
    public weddingDate?: Date,
    public receptionTime?: Date,
    public weddingTime?: Date,
    public user?: IUser | null,
  ) {}
}
