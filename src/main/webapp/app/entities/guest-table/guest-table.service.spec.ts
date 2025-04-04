import axios from 'axios';
import sinon from 'sinon';

import GuestTableService from './guest-table.service';
import { GuestTable } from '@/shared/model/guest-table.model';

const error = {
  response: {
    status: null,
    data: {
      type: null,
    },
  },
};

const axiosStub = {
  get: sinon.stub(axios, 'get'),
  post: sinon.stub(axios, 'post'),
  put: sinon.stub(axios, 'put'),
  patch: sinon.stub(axios, 'patch'),
  delete: sinon.stub(axios, 'delete'),
};

describe('Service Tests', () => {
  describe('GuestTable Service', () => {
    let service: GuestTableService;
    let elemDefault;

    beforeEach(() => {
      service = new GuestTableService();
      elemDefault = new GuestTable(123, 'AAAAAAA', 0, 'AAAAAAA', false, 'CONFIRMED', 'AAAAAAA', 'GROOM_FAMILY', 0, 0, 'AAAAAAA', false);
    });

    describe('Service methods', () => {
      it('should find an element', async () => {
        const returnedFromService = { ...elemDefault };
        axiosStub.get.resolves({ data: returnedFromService });

        return service.find(123).then(res => {
          expect(res).toMatchObject(elemDefault);
        });
      });

      it('should not find an element', async () => {
        axiosStub.get.rejects(error);
        return service
          .find(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should create a GuestTable', async () => {
        const returnedFromService = { id: 123, ...elemDefault };
        const expected = { ...returnedFromService };

        axiosStub.post.resolves({ data: returnedFromService });
        return service.create({}).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not create a GuestTable', async () => {
        axiosStub.post.rejects(error);

        return service
          .create({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should update a GuestTable', async () => {
        const returnedFromService = {
          lastNameAndFirstName: 'BBBBBB',
          numberOfSeats: 1,
          phone: 'BBBBBB',
          nearDanceFloor: true,
          status: 'BBBBBB',
          side: 'BBBBBB',
          relation: 'BBBBBB',
          notWithId: 1,
          withId: 1,
          conditions: 'BBBBBB',
          accessibility: true,
          ...elemDefault,
        };

        const expected = { ...returnedFromService };
        axiosStub.put.resolves({ data: returnedFromService });

        return service.update(expected).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not update a GuestTable', async () => {
        axiosStub.put.rejects(error);

        return service
          .update({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should partial update a GuestTable', async () => {
        const patchObject = {
          lastNameAndFirstName: 'BBBBBB',
          nearDanceFloor: true,
          relation: 'BBBBBB',
          conditions: 'BBBBBB',
          accessibility: true,
          ...new GuestTable(),
        };
        const returnedFromService = Object.assign(patchObject, elemDefault);

        const expected = { ...returnedFromService };
        axiosStub.patch.resolves({ data: returnedFromService });

        return service.partialUpdate(patchObject).then(res => {
          expect(res).toMatchObject(expected);
        });
      });

      it('should not partial update a GuestTable', async () => {
        axiosStub.patch.rejects(error);

        return service
          .partialUpdate({})
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should return a list of GuestTable', async () => {
        const returnedFromService = {
          lastNameAndFirstName: 'BBBBBB',
          numberOfSeats: 1,
          phone: 'BBBBBB',
          nearDanceFloor: true,
          status: 'BBBBBB',
          side: 'BBBBBB',
          relation: 'BBBBBB',
          notWithId: 1,
          withId: 1,
          conditions: 'BBBBBB',
          accessibility: true,
          ...elemDefault,
        };
        const expected = { ...returnedFromService };
        axiosStub.get.resolves([returnedFromService]);
        return service.retrieve({ sort: {}, page: 0, size: 10 }).then(res => {
          expect(res).toContainEqual(expected);
        });
      });

      it('should not return a list of GuestTable', async () => {
        axiosStub.get.rejects(error);

        return service
          .retrieve()
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });

      it('should delete a GuestTable', async () => {
        axiosStub.delete.resolves({ ok: true });
        return service.delete(123).then(res => {
          expect(res.ok).toBeTruthy();
        });
      });

      it('should not delete a GuestTable', async () => {
        axiosStub.delete.rejects(error);

        return service
          .delete(123)
          .then()
          .catch(err => {
            expect(err).toMatchObject(error);
          });
      });
    });
  });
});
