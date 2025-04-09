package com.project.withus.controller;

import com.project.withus.domain.couple.Couple;
import com.project.withus.domain.letter.Letter;
import com.project.withus.domain.user.User;
import com.project.withus.repository.UserRepository;
import com.project.withus.service.CoupleService;
import com.project.withus.service.LetterService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("/letters")
public class LetterController {

    private final UserRepository userRepository;
    private final CoupleService coupleService;
    private final LetterService letterService;

    private User getLoginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("LOGIN_USER");
    }

    private String redirectIfNotLoggedIn(User user) {
        return (user == null) ? "redirect:/login?error=session" : null;
    }

    private String redirectIfNotCoupled(User user) {
        return coupleService.isNotCoupled(user) ? "redirect:/?noCouple=true" : null;
    }

    @GetMapping("/received")
    public String receivedLetters(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);
        model.addAttribute("currentURI", request.getRequestURI());

        String redirect = redirectIfNotLoggedIn(user);
        if (redirect != null) return redirect;

        redirect = redirectIfNotCoupled(user);
        if (redirect != null) return redirect;

        List<Letter> letters = letterService.getReceivedLetters(user);
        model.addAttribute("receivedLetters", letters);
        return "letters/received";
    }

    @GetMapping("/sent")
    public String sentLetters(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);
        model.addAttribute("currentURI", request.getRequestURI());

        String redirect = redirectIfNotLoggedIn(user);
        if (redirect != null) return redirect;

        redirect = redirectIfNotCoupled(user);
        if (redirect != null) return redirect;

        List<Letter> letters = letterService.getSentLetters(user);
        model.addAttribute("sentLetters", letters);
        model.addAttribute("type", "sent");
        return "letters/sent";
    }

    @GetMapping("/write")
    public String writeLetterForm(HttpServletRequest request, Model model) {
        User user = getLoginUser(request);
        model.addAttribute("currentURI", request.getRequestURI());

        String redirect = redirectIfNotLoggedIn(user);
        if (redirect != null) return redirect;

        redirect = redirectIfNotCoupled(user);
        if (redirect != null) return redirect;

        return "letters/write";
    }

    @PostMapping("/send")
    public String sendLetter(
            @RequestParam String content,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime unlockDate,
            HttpServletRequest request) {

        User me = getLoginUser(request);
        String redirect = redirectIfNotLoggedIn(me);
        if (redirect != null) return redirect;

        Couple couple = coupleService.getMyCouple(me)
                .orElseThrow(() -> new RuntimeException("커플이 연결되지 않았어요!"));
        User partner = coupleService.getPartner(me, couple);

        letterService.sendLetter(me, partner, content, unlockDate);
        return "redirect:/letters/sent";
    }

    @GetMapping("/view/{id}")
    public String readLetter(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {

        User user = getLoginUser(request);
        model.addAttribute("currentURI", request.getRequestURI());

        String redirect = redirectIfNotLoggedIn(user);
        if (redirect != null) return redirect;

        try {
            Letter letter = letterService.readLetter(user, id);

            boolean isLocked = user.getId().equals(letter.getReceiver().getId()) &&
                    letter.getUnlockDate() != null &&
                    letter.getUnlockDate().isAfter(LocalDateTime.now());

            model.addAttribute("letter", letter);
            model.addAttribute("locked", isLocked);
            return "letters/view";

        } catch (RuntimeException e) {
            List<Letter> letters = letterService.getReceivedLetters(user);
            model.addAttribute("receivedLetters", letters);
            model.addAttribute("errorMessage", e.getMessage());
            return "letters/received";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteLetter(
            @PathVariable Long id,
            Model model,
            HttpServletRequest request) {

        User user = getLoginUser(request);
        model.addAttribute("currentURI", request.getRequestURI());

        String redirect = redirectIfNotLoggedIn(user);
        if (redirect != null) return redirect;

        try {
            boolean isSender = letterService.deleteLetter(user, id);
            return isSender ? "redirect:/letters/sent" : "redirect:/letters/received";

        } catch (RuntimeException e) {
            Letter letter = letterService.findById(id);
            model.addAttribute("letter", letter);
            model.addAttribute("errorMessage", e.getMessage());
            return "letters/view";
        }
    }
}