<template>
  <v-row style="width: 100%; height: 91.5vh;">
    <div class="nav" id="nav-box">
      <v-navigation-drawer height="100%" permanent>
        <div id="myProfile">
          <div>
            <img id="myImage" :src="'https://joinmeeting.tk/images/'+this.$store.state.myPicture" />
          </div>
          <v-list-item-content class="item-content">
            <v-list-item-title class="myName">
              <h5>{{ $store.state.myName }}</h5>
            </v-list-item-title>
            <v-list-item-subtitle>
              <div id="userIdBox">
                <p style="font-size:14px;">{{ $store.state.userId }}</p>
              </div>
            </v-list-item-subtitle>
          </v-list-item-content>
        </div>
        <v-divider></v-divider>
        <v-list height="50vh" nav dense>
          <v-list-item-group v-model="group" active-class="border" color="orange">
            <div id="v-list-item-box">
              <v-list-item
                v-for="(group, i) in this.$store.state.myGroups"
                :key="group.groupNo"
                @click="toggle({'i':i, 'groupNo': group.groupNo})"
              >
                <v-list-item-icon style="margin-left:8px; margin-right:22px;">
                  <v-icon dark>mdi-account-multiple</v-icon>
                </v-list-item-icon>

                <v-list-item-content>
                  <v-badge
                    v-if="$store.state.userId === group.hostId"
                    color="red"
                    content="HOST"
                    overlap
                    offset-x="51"
                    offset-y="20"
                  >
                    <div id="groupNameText" v-text="group.groupName"></div>
                  </v-badge>
                  <v-badge v-else color="rgb(0, 0, 0, 0)" dot overlap>
                    <div id="groupNameText" v-text="group.groupName"></div>
                  </v-badge>
                  <!-- <div id="groupNameText" v-text="group.groupName"></div> -->
                </v-list-item-content>
                <!-- <v-icon color="white">mdi-key</v-icon> -->
                <!-- <v-icon  v-if="$store.state.userId === group.hostId" color="white">mdi-key-variant</v-icon> -->
                <!-- <v-badge v-if="$store.state.userId === group.hostId" color="red" content="HOST" overlap offset-x="" offset-y="15">
                <v-list-item-content>
                  <p id="groupNameText" style="padding-top: 5px" v-text="group.groupName"></p>
                </v-list-item-content>
              </v-badge>
          
              <v-badge v-else color="rgb(0, 0, 0, 0)" dot overlap offset-x="25" offset-y="15">
                <v-list-item-content>
                  <p id="groupNameText" style="padding-top: 5px" v-text="group.groupName"></p>
                </v-list-item-content>
                </v-badge>-->
              </v-list-item>
            </div>
          </v-list-item-group>

        </v-list>
          <v-btn id="groupCreate" @click="modalOn= !modalOn" dark class="ml-7 mt-3">
            <v-icon class="mr-2" medium>mdi-account-multiple-plus</v-icon>
            <p style="margin: 0px;">그룹 만들기</p>
          </v-btn>
          <v-dialog v-model="modalOn" max-width="500px">
            <CreateGroup :modalOn=modalOn @close="closeModal"/>
          </v-dialog>
      </v-navigation-drawer>
    </div>

    <v-col
      v-if="$store.state.myGroups && $store.state.myGroups.length !== 0 "
      style="margin-left : 20px;"
    >
      <GroupContent
        :groupInfo="$store.state.myGroups[this.onboarding]"
        :meetingNoteInfo="meetingNoteInfo"
        :conferenceAlert="conferenceAlert"
        :tmpGroupNo="tmpGroupNo"
        :nowMeeting="nowMeeting"
      />
    </v-col>
    <v-col v-else>
      <EmptyGroup />
    </v-col>
  </v-row>
</template>

<script>
import GroupContent from '../components/Group/GroupContent.vue';
import CreateGroup from '../components/Group/CreateGroup.vue';
import EmptyGroup from '../components/Group/EmptyGroup.vue';

import SERVER from '../api/spring.js';

import SockJS from 'sockjs-client';
import Stomp from 'webstomp-client';
import Axios from 'axios';

export default {
  name: 'Groups',
  components: {
    GroupContent,
    CreateGroup,
    EmptyGroup
  },
  data() {
    return {
      group: {},
      modalOn: false,
      onboarding: 0,
      sock: null,
      ws: null,
      reconnect: 0,
      recv: '',
      meetingNoteInfo: [],
      conferenceAlert: false,
      tmpGroupNo: null,
      nowMeeting : null
    };
  },
  methods: {

    toggle({i, groupNo}) {
      let meetingList = [];
      const calendar_meeting = [];
      this.conferenceAlert = false;

      Axios.get(SERVER.URL +'/meeting/get/group/'+groupNo)
        .then((res)=> {
          meetingList = res.data.meetings;
          meetingList.forEach(meeting => {
            this.nowMeeting = meeting.hasMeeting;
            Axios.get(SERVER.URL +'/note/getno/' + groupNo + '/' + meeting.meetingNo)
              .then((res)=> {
                // console.log('result', meeting, res);

                calendar_meeting.push({
                  name: meeting.title,
                  start: meeting.createdDate,
                  end: meeting.modifiedDate,
                  groupNo: meeting.groupNo,
                  color: 'blue',
                  timed: false,
                  id: res.data.noteNo,
                  isNote: res.data.isNote,

                });
              }).catch();
          });
        })
        .catch(err=>console.error(err));
      
      this.meetingNoteInfo = calendar_meeting;
      this.ws.disconnect();
      this.onboarding = i;
      this.connect(this.onboarding);
    },

    createGroup() {
      this.$store.state.groupModalOn = true;
    },
    closeModal() {
      this.modalOn = false;
    },

    connect(i) {
      this.sock = new SockJS(SERVER.URL2);
      this.ws = Stomp.over(this.sock);
      this.ws.connect(
        { token: this.$store.state.accessToken },
        frame => {
          this.ws.subscribe(
            '/send/meeting/' + this.$store.state.myGroups[i].groupNo,
            res => {
              this.conferenceAlert = true;
              this.recv = res.body;
              this.tmpGroupNo = JSON.parse(this.recv).groupNo;
            }
          );
        },
        () => {
          if (this.reconnect++ <= 5) {
            setTimeout(() => {
              this.sock = new SockJS(SERVER.URL2);
              this.ws = Stomp.over(this.sock);
              this.connect(i);
            }, 1 * 1000);
          }
        }
      );
    }
  },
  mounted() {
    let meetingList = [];
    const calendar_meeting = [];
    
    Axios
      .get(SERVER.URL + SERVER.ROUTES.getGroupInfo + '/' + this.$store.state.userId)
      .then(res => {
        this.$store.commit('SET_GROUP_INFO', res);
        if (this.$store.state.myGroups && this.$store.state.myGroups.length != 0) {
          this.connect(this.onboarding);
        }
        const groupNo = res.data.groups[0].groupNo;
        Axios.get(SERVER.URL +'/meeting/get/group/'+groupNo)
          .then((res)=> {
            meetingList = res.data.meetings;
            meetingList.forEach(meeting => {
              Axios.get(SERVER.URL +'/note/getno/' + groupNo + '/' + meeting.meetingNo)
                .then((res)=> {
                  // console.log(meeting.createdDate);
                  calendar_meeting.push({
                    name: meeting.title,
                    start: meeting.createdDate,
                    end: meeting.modifiedDate,
                    groupNo: meeting.groupNo,
                    color: 'blue',
                    timed: false,
                    id: res.data.noteNo,
                    isNote: res.data.isNote,
                  });
                }).catch();
            });
            this.meetingNoteInfo = calendar_meeting;
          })
          .catch(err=>console.error(err));
      });     
  },
  created() {
    this.sock = new SockJS(SERVER.URL2);
    this.ws = Stomp.over(this.sock);
  },

  destroyed() {
    this.ws.disconnect();
  }
};
</script>

<style >
.nav {
  padding: 0;
}

#nav-box {
  margin-left: 15px;
}
.v-navigation-drawer__content {
  background-color: rgb(52, 63, 87);
  height: 100%;
  overflow-y: hidden;
}

#myProfile {
  width: 100%;
  text-align: center;
}

#myImage {
  margin-top: 20px;
  display: inline-block;
  width: 100px;
  height: 100px;
  object-fit: cover;
  border-radius: 50%;
}
.myName {
  word-wrap: break-word;
  display: inline-block;
}
.myName h5 {
  color: white;
  word-wrap: break-word;
  display: inline-block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 200px;
  height: 25px;
}
#userIdBox {
  display: inline-block;
  word-wrap: break-word;
}

#userIdBox p {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 200px;
  height: 20px;
  font-size: 16px;
  color: white;
}

#userIdBox p:hover {
  text-overflow: clip;
  white-space: normal;
  word-break: break-all;
}

#groupNameText {
  color: white;
  width: 100px;
  font-size: 20px;
  left: -20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
#v-list-item-box {
  height: 400px;
  overflow-y: auto;
}

#groupCreate {
  margin-left: 20px;
  background-color: orange;
  height: 45px;
  width: 200px;
  position: absolute;
}
#groupCreate p {
  font-size: 20px;
  font-weight: 550;
}

#GroupContentBox {
  display: inline-block;
  position: absolute;
  left: 241px;
  height: 100%;
  right: 0;
  overflow: hidden;
  /* background-color: aqua; */
}
#GroupContentBox .row {
  margin-left: 10px;
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>
