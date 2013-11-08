(ns closerve-example.core
  (:require [ring.util.response :as response]
            [ring.middleware.params :as params]
            [ring.util.response :as response]
            [clojure.core.async :refer [go close! >!! <! >! sliding-buffer chan]]
            )
  (:use [closerve state wscmd server liftutil]
        [hickory render])
  (:gen-class))


(register-lift-snippet
 "TestSlowLoad"
 [node req page-id lift-instr]
 (Thread/sleep 5000)
 (-> node 
     (assoc :content ["Lazy Load finished!"])
     (assoc-in [:attrs :class] "alert alert-success")
     )
 )


(register-lift-snippet
 "ActiveIfIsMkrrf"
 [node req page-id lift-instr]
 (if (and (:server-name req) 
          (re-matches #"^.*mkrrf-it.com$" (:server-name req)))
   node "")
 )

(def chat-chans (atom #{}))
(def chat-msgs (atom []))

(register-cmd-proc-fn 
 "ChatInput"
 [context cmd]
 (let [chat-msg (cmd "chatmsg")]
   (if (string? chat-msg)     
     (do 
       (swap! chat-msgs #(apply vector (drop (- (count %1) 9)
                                             (conj %1 %2))) chat-msg)
       (send-cmd-to-page (:page-id context) 
                         {:act :reset 
                          :selector 
                          (str "#" (:form-id context))})
       )       
     ))
 )


(register-cmd-proc-fn 
 "ChatConfirm"
 [context cmd]
 (let [chatform {:type :element :tag :form
                 :attrs {:class "input-group lift:form.ajax?callback=ChatInput"}
                 :content [{:type :element :tag :input
                            :attrs {:type "text" :name "chatmsg" :class "form-control"}}
                           {:type :element :tag :span
                            :attrs {:class "input-group-btn"}
                            :content [{:type :element :tag :input
                                       :attrs {:value "GO!" :class "btn btn-default" :type "submit"}}]}]}
       newnode (process-snippets chatform (:req context) (:page-id context))
       htmltxt (hickory-to-html newnode)]
   (send-cmd-to-page (:page-id context)
                     {:act :replaceWith
                      :selector (str "#" (:form-id context))
                      :html htmltxt
                      }))
 )

(register-cmd-proc-fn 
 "MakeAlert"
 [context cmd]
 (send-cmd-to-page (:page-id context)
                   {:act :globalEval
                    :code "alert('Alert from Server!');"}))



(add-watch chat-msgs :watch-chat-msgs
           (fn [key aref old-val new-val]
             (if new-val 
               (doseq [c @chat-chans]
                 (go (>! c (last new-val)))))))

(register-comet-fn
 "chat"
 [ch req page-id uuid]
 (swap! chat-chans conj ch)
 (loop [msg (<! ch)]
   (when msg 
     (send-cmd-to-page page-id 
                       {:act :append
                        :selector "#msglist"
                        :html (hickory-to-html {:type :element, :attrs nil
                                                :tag :li :content [msg]}) })
     (recur (<! ch))))
 (prn "chat channel is closed")
 (swap! chat-chans disj ch)
)

;;;rest example

(register-dyna-action 
 #"^/sum/([\d]+)/([\d]+)$"
 [req op1 op2] 
 (response/response (str "sum = " (+ (Long. op1) (+ (Long. op2 ))))))


;;;"no .html here"
(reset! access-rules
        [         
         [#"^/templates-hidden.*$" (fn [req] false)]
         ])

(defn -main 
  [serverdir & {:strs [host port dbhost]
                :or {host "127.0.0.1" port "9090" dbhost "localhost"}}]

  (reset! root-dir serverdir)
  (start-server host (Integer. port)))
